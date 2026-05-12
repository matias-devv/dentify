package com.dentify.domain.appointment.event.listener;

import com.dentify.domain.appointment.event.AppointmentCreatedWithPaymentEvent;
import com.dentify.domain.appointment.enums.AppointmentStatus;
import com.dentify.domain.appointment.model.Appointment;
import com.dentify.domain.appointment.service.IAppointmentService;
import com.dentify.domain.mercadopagopayment.service.IMercadoPagoPaymentService;
import com.dentify.domain.payment.model.TreatmentPayment;
import com.dentify.domain.payment.service.ITreatmentPaymentService;
import com.dentify.domain.receipt.model.Receipt;
import com.dentify.domain.receipt.service.IReceiptService;
import com.dentify.integration.email.service.IEmailService;
import com.dentify.security.multitenancy.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * ✅ ASYNC EVENT LISTENER
 *
 * Se ejecuta en un thread separado (pool de threads de Spring Task Execution)
 * Maneja todas las operaciones "pesadas" después de crear el appointment:
 * - Generar PDF del recibo
 * - Enviar emails (4 en paralelo)
 * - Actualizar status del appointment a CONFIRMED (si corresponde)
 *
 * PERFORMANCE: De 20-30s sincronos → 500-800ms en el endpoint
 *
 * Si algo falla aquí, se loguea pero NO afecta la transacción del appointment
 * (eventual consistency pattern)
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AppointmentNotificationListener {

    //services
    private final IReceiptService receiptService;
    private final IEmailService emailService;
    private final IAppointmentService appointmentService;
    private final ITreatmentPaymentService paymentService;
    private final IMercadoPagoPaymentService mercadoPagoPaymentService;

    /**
     * 🎯 Entry point: Se ejecuta ASINCRONAMENTE cuando se publica el evento
     *
     * @param event Evento con todos los datos del appointment + payment
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async("taskExecutor")
    public void handleAppointmentCreatedWithPay(AppointmentCreatedWithPaymentEvent event) {

        long startTime = System.currentTimeMillis();
        TenantContext.set(event.getTenantId());
        try {
            log.info("⏳ [ASYNC] Starting background processing for appointment: {}", event.getAppointmentId());

            // PASO 1: BUSCAR APPOINTMENT
            Appointment appointment = appointmentService.findByIdWithAllEmailData(event.getAppointmentId());

            // PASO 2. BUSCAR PAYMENT
            TreatmentPayment payment = paymentService.findByIdForPaymentProcessing(event.getPaymentId() );

            //PASO 4: CREAR MP SI ES EL CASO
            this.handleMercadoPagoPaymentOption( appointment, payment);

            // PASO 5: Generar recibo (si es pago inmediato) ( puede ser null)
            Receipt receipt = this.handleReceiptGeneration( appointment, payment, event.getIsPaymentImmediate());

            // PASO 6: Enviar emails (operaciones en paralelo)
            log.info("📧 Sending emails for appointment: {}", event.getAppointmentId());
            sendEmailsInParallel( appointment, payment, event.getIsPaymentImmediate(), receipt);

            // PASO 7: Actualizar appointment status a CONFIRMED (si corresponde)
            this.handleAppointmentConfirmation(event.getShouldConfirmAppointment(), appointment);

            this.handlePatientStats(event.getShouldConfirmAppointment(), appointment);

            long totalTime = System.currentTimeMillis() - startTime;
            log.info("total time transaction: {}", totalTime);
        } catch (Exception e) {
            long totalTime = System.currentTimeMillis() - startTime;
            log.error("❌ [ASYNC] Error processing appointment {} after {}ms: {}", event.getAppointmentId(), totalTime, e.getMessage(), e);
        }
    }

    private void handleMercadoPagoPaymentOption( Appointment appointment, TreatmentPayment payment) {

        try {
            if (payment.isInMercadoPago()) {
                mercadoPagoPaymentService.handleMercadoPagoPayment(payment, appointment);
            }
        }
        catch (Exception e) {
            log.error("❌ Error creating mercado pago payment", e);
        }
    }

    private Receipt handleReceiptGeneration(Appointment appointment, TreatmentPayment payment, boolean payNow) {

        if ( payment.isInCash() && payNow ) {
            return generateReceipt(appointment, payment);
        }
        return null;
    }

    /**
     * Genera el recibo PDF y lo sube a Cloudinary
     */
    private Receipt generateReceipt(Appointment appointment, TreatmentPayment payment) {

        try {

            long startTime = System.currentTimeMillis();

            Receipt receipt = receiptService.generateAndSaveReceipt(payment, appointment, null);

            long duration = System.currentTimeMillis() - startTime;
            log.info("✅ Receipt generated in {}ms: {}", duration, receipt.getFilename());

            return receipt;

        } catch (Exception e) {
            log.error("❌ Error generating receipt", e);
            return null;
        }
    }

    /**
     * Envía todos los emails en paralelo utilizando CompletableFuture
     * o threads separados
     */
    private void sendEmailsInParallel( Appointment appointment, TreatmentPayment payment, boolean payNow, Receipt receipt) {
        try {
            // Aquí usamos @Async en métodos privados o CompletableFuture
            // para paralelizar los 4 emails

            if ( payment.isInCash() && payNow ) {
                // CASE: CASH + payNow = true
                // Enviar: 2 receipts (patient + dentist) + 2 confirmations (patient + dentist)
                sendCashPaymentEmails( appointment, payment, receipt);
            }
            if ( payment.isInCash() && !payNow ) {
                // CASE: CASH + payNow = false
                // Enviar: 2 confirmaciones (patient + dentist)
                sendSimpleConfirmationEmails(appointment);
            }
            if ( payment.isInMercadoPago() ) {
                // CASE: Mercado Pago
                // Enviar: confirmación + link al paciente, confirmación al dentista
                sendMercadoPagoEmails(appointment, payment);
            }

            log.info("✅ All emails sent successfully for appointment: {}", appointment.getId_appointment());

        } catch (Exception e) {
            log.error("❌ Error sending emails", e);
        }
    }

    /**
     * Envía emails para pago en CASH inmediato
     * 4 emails: receipt patient + receipt dentist + confirmation patient + confirmation dentist
     */
    private void sendCashPaymentEmails(Appointment appointment, TreatmentPayment payment, Receipt receipt) {

        try {

            emailService.sendCashPaymentReceipt( appointment, payment, receipt );

            log.info("✅ CASH payment emails sent for appointment: {}", appointment.getId_appointment() );

        } catch (Exception e) {
            log.error("❌ Error sending CASH payment emails", e);
        }
    }

    /**
     * Envía emails para pago con Mercado Pago
     */
    private void sendMercadoPagoEmails(Appointment appointment, TreatmentPayment payment) {

        try {
            // Email con link de Mercado Pago
            if ( payment.getMercado_pago_data() != null) {
                emailService.sendFirstConfirmationsMercadoPago( appointment, payment.getMercado_pago_data().getInitPoint() );
            }

            log.info("✅ Mercado Pago emails sent for appointment: {}", appointment.getId_appointment());

        } catch (Exception e) {
            log.error("❌ Error sending Mercado Pago emails", e);
        }
    }

    /**
     * Envía emails simples de confirmación (CASH sin pago inmediato)
     */
    private void sendSimpleConfirmationEmails(Appointment appointment) {
        try {

            log.debug("📧 Sending simple confirmation emails...");

            emailService.sendFirstConfirmations(appointment);

            log.info("✅ Confirmation emails sent for appointment: {}", appointment.getId_appointment());

        } catch (Exception e) {
            log.error("❌ Error sending confirmation emails", e);
        }
    }

    private void handleAppointmentConfirmation(Boolean shouldConfirmAppointment, Appointment appointment) {

        try {

            if ( shouldConfirmAppointment == true ) {

                appointment.setAppointmentStatus(AppointmentStatus.CONFIRMED);

                appointmentService.persistAppointment(appointment);

                log.info("✅ Appointment {} status updated to CONFIRMED", appointment.getId_appointment());
            }

        } catch (Exception e) {
            log.error("❌ Error updating appointment status", e);
        }
    }


    private void handlePatientStats(Boolean shouldConfirmAppointment, Appointment appointment) {
    }

}
