package com.dentify.integration.email;


import com.dentify.domain.appointment.model.Appointment;
import com.dentify.domain.dentist.service.IDentistService;
import com.dentify.domain.invitation.dto.response.CreateInvitationResponse;
import com.dentify.domain.invitation.enums.InvitedRole;
import com.dentify.domain.invitation.model.Invitation;
import com.dentify.domain.mercadopagopayment.model.MercadoPagoPayment;
import com.dentify.domain.notification.enums.PaymentNotificationConfig;
import com.dentify.domain.notification.model.Notification;
import com.dentify.domain.notification.service.INotificationService;
import com.dentify.domain.patient.service.IPatientService;
import com.dentify.domain.pay.enums.PaymentMethod;
import com.dentify.domain.pay.enums.PaymentStatus;
import com.dentify.domain.notification.enums.ReminderWindow;
import com.dentify.domain.pay.model.Pay;
import com.dentify.domain.receipt.model.Receipt;
import com.dentify.domain.userProfile.model.UserProfile;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.InputStream;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailService {

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${notifications.email.enabled:true}")
    private boolean emailEnabled;

    @Value("${app.base-url}")
    private String baseUrl;

    @Value("${clinic.logo.url:}")
    private String clinicLogoUrl;

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final INotificationService notificationService;
    private final IPatientService patientService;
    private final IDentistService dentistService;

    private static final DateTimeFormatter DATE_FORMATTER     = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FORMATTER     = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // =========================================================================
    // INVITATIONS
    // =========================================================================

    /**
     * Sends an invitation email to a new user (dentist or secretary).
     *
     * @param invitation  the invitation entity (token, role, email)
     * @param inviter     UserProfile of whoever sent the invitation (name, clinic)
     * @param isAdmin     To determine whether to send dentifyInvitation or common Invitation
     */
    public void sendInvitation(Invitation invitation, UserProfile inviter, boolean isAdmin) {
        if (!emailEnabled) return;

        try {
            String email = invitation.getEmail();

            if (this.isBlank(email)) {
                log.warn("Invitation without email - Invitation id: {}", invitation.getId());
                return;
            }

            String roleName = invitation.getInvitedRole() == InvitedRole.DENTIST
                    ? "Odontólogo/a"
                    : "Secretario/a";

            Map<String, Object> data = new HashMap<>();

            // Clinic
            data.put("clinicName",  inviter.getClinic().getName());
            data.put("clinicEmail", inviter.getClinic().getEmail());

            // Inviter
            data.put("inviterName", inviter.getName() + " " + inviter.getSurname());

            // Invitation
            data.put("invitedRole",       roleName);
            data.put("invitedEmail",      email);
            data.put("registrationUrl",   buildRegistrationUrl(invitation.getToken()));

            // Footer
            data.put("currentYear", java.time.Year.now().getValue());
            data.put("baseUrl",     baseUrl);

            String template = isAdmin ? "email/dentify-invitation" : "email/common-invitation";

            String subject = isAdmin
                    ? "Tu acceso a Dentify está listo"
                    : "Te invitaron a unirte a " + inviter.getClinic().getName();

            sendEmail(email, subject, template, data);

            log.info("Invitation email sent to: {} | role: {} | template: {}", email, roleName, template);

        } catch (Exception e) {
            log.error("Error sending invitation email to {} - Invitation {}: {}",
                    invitation.getEmail(), invitation.getId(), e.getMessage());
        }
    }


    private String buildRegistrationUrl(String token) {
        return String.format("%s/registro?token=%s", baseUrl, token);
    }

    // =========================================================================
    // CANCELLATIONS
    // =========================================================================

    /**
     * Manual cancellation — notifies BOTH patient and dentist.
     * Policy: notification → silent (log only, never propagate).
     */
    public void sendAppointmentManuallyCancelled(Appointment appointment) {
        if (!emailEnabled) return;

        String reason = appointment.getReason_for_cancellation() != null
                ? appointment.getReason_for_cancellation()
                : "El turno fue cancelado por la clínica.";

        String cancelledBy = appointment.getAppointmentStatus().name();

        notifyPatientCancellation(appointment, reason, cancelledBy);
        notifyDentistCancellation(appointment, reason, cancelledBy);
    }

    private void notifyPatientCancellation(Appointment appointment, String reason, String cancelledBy) {
        try {

            String email =  appointment.getPatient().getEmail();

            patientService.validateEmailPatient( email );

            Map<String, Object> data = buildBaseEmailData(appointment);
            data.put("cancellationReason", reason);
            data.put("cancelledBy", cancelledBy);
            data.put("isPatient", true);

            sendEmail(email,
                    "Turno cancelado - " + extractClinicName(appointment),
                    "email/appointment-cancelled-manual-patient",
                    data);

            log.info("Cancellation email sent to patient: {}", email);

        } catch (Exception e) {
            log.error("Error sending cancellation email to patient - Appointment {}: {}",
                    appointment.getId_appointment(), e.getMessage());
        }
    }

    private void notifyDentistCancellation(Appointment appointment, String reason, String cancelledBy) {
        try {

            String email = extractDentistEmail(appointment);

            dentistService.validateDentistEmail(email);

            Map<String, Object> data = buildBaseEmailData(appointment);
            data.put("cancellationReason", reason);
            data.put("cancelledBy", cancelledBy);
            data.put("isPatient", false);

            sendEmail(email,
                    "Turno cancelado - Paciente: " + extractPatientFullName(appointment),
                    "email/appointment-cancelled-manual-dentist",
                    data);

            log.info("Cancellation email sent to dentist: {}", email);

        } catch (Exception e) {
            log.error("Error sending cancellation email to dentist - Appointment {}: {}",
                    appointment.getId_appointment(), e.getMessage());
        }
    }

    /**
     * System cancellation (non-payment) — patient only, silent policy.
     */
    public void sendAppointmentCancelledBySystem(Appointment appointment) {
        if (!emailEnabled) return;

        try {

            String email =  appointment.getPatient().getEmail();

            patientService.validateEmailPatient( email );

            Map<String, Object> data = buildBaseEmailData(appointment);
            data.put("cancellationReason", "Turno cancelado por falta de pago.");

            sendEmail(email,
                    " Turno cancelado por falta de pago",
                    "email/appointment-cancelled-by-system",
                    data);

            log.info("System cancellation email sent to: {}", email);

        } catch (Exception e) {
            log.error("Error sending system cancellation email - Appointment {}: {}",
                    appointment.getId_appointment(), e.getMessage());
        }
    }

    // =========================================================================
    // CONFIRMATIONS AND REMINDERS
    // =========================================================================

    /**
     * 48hs reminder — dispatches by payment method.
     */
    public void sendConfirmationRequest(Notification notification) {

        Appointment appointment = notification.getAppointment();

        PaymentMethod method = appointment.getPrimaryPaymentMethod();

        if (method == PaymentMethod.MERCADO_PAGO) {
            sendConfirmationWithPaymentOption(notification, appointment);
        } else {
            sendConfirmationCashOnly(notification, appointment);
        }
    }

    private void sendConfirmationWithPaymentOption(Notification notification, Appointment appointment) {
        try {

            String email =  appointment.getPatient().getEmail();

            patientService.validateEmailPatient( email );

            Pay pay = appointment.getPrimaryPayment();

            Map<String, Object> data = buildBaseEmailData(appointment);

            addPaymentData(data, pay);

            data.put("confirmationUrl", buildConfirmationUrl(notification));

            if (pay.getMercado_pago_data() != null) addMercadoPagoData(data, pay.getMercado_pago_data());

            sendEmail(email, " Confirmá tu asistencia - Turno en 2 días",
                    "email/confirmation-request-mercadopago-48h", data);

            log.info("Confirmation email (MERCADO_PAGO) sent to: {}", email);

        } catch (Exception e) {
            log.error("Error sending confirmation (MERCADO_PAGO) - Appointment {}: {}",
                    appointment.getId_appointment(), e.getMessage());
        }
    }

    private void sendConfirmationCashOnly(Notification notification, Appointment appointment) {
        try {

            String email =  appointment.getPatient().getEmail();

            patientService.validateEmailPatient( email );

            Pay pay = appointment.getPrimaryPayment();

            Map<String, Object> data = buildBaseEmailData(appointment);

            addPaymentData(data, pay);

            data.put("confirmationUrl", buildConfirmationUrl(notification));

            sendEmail(email, "Confirmá tu asistencia - Turno en 2 días",
                    "email/confirmation-request-cash-48h", data);

            log.info("Confirmation email (CASH) sent to: {}", email);

        } catch (Exception e) {
            log.error("Error sending confirmation (CASH) - Appointment {}: {}",
                    appointment.getId_appointment(), e.getMessage());
        }
    }

    /**
     * 24hs urgent reminder — dispatches by payment method.
     */
    public void urgentConfirmationRequest(Notification notification) {

        Appointment appointment = notification.getAppointment();

        PaymentMethod method = appointment.getPrimaryPaymentMethod();

        if (method == PaymentMethod.MERCADO_PAGO) {
            sendUrgentConfirmationWithPaymentOption(notification, appointment);
        } else {
            sendUrgentConfirmationCashOnly(notification, appointment);
        }
    }

    private void sendUrgentConfirmationWithPaymentOption(Notification notification, Appointment appointment) {
        try {

            String email =  appointment.getPatient().getEmail();

            patientService.validateEmailPatient( email );

            Pay pay = appointment.getPrimaryPayment();

            Map<String, Object> data = buildBaseEmailData(appointment);

            addPaymentData(data, pay);

            data.put("confirmationUrl", buildConfirmationUrl(notification));

            if (pay.getMercado_pago_data() != null) addMercadoPagoData(data, pay.getMercado_pago_data());

            sendEmail(email, "URGENTE - Confirmá tu turno o se cancelará",
                    "email/urgent-confirmation-request-mercadopago-24h", data);

            log.info("Urgent confirmation (MERCADO_PAGO) sent to: {}", email);

        } catch (Exception e) {
            log.error("Error sending urgent confirmation (MERCADO_PAGO) - Appointment {}: {}",
                    appointment.getId_appointment(), e.getMessage());
        }
    }

    private void sendUrgentConfirmationCashOnly(Notification notification, Appointment appointment) {
        try {

            String email =  appointment.getPatient().getEmail();

            patientService.validateEmailPatient( email );

            Pay pay = appointment.getPrimaryPayment();

            Map<String, Object> data = buildBaseEmailData(appointment);

            addPaymentData(data, pay);

            data.put("confirmationUrl", buildConfirmationUrl(notification));

            sendEmail(email, "URGENTE - Confirmá tu turno o se cancelará",
                    "email/urgent-confirmation-request-cash-24h", data);

            log.info("Urgent confirmation (CASH) sent to: {}", email);

        } catch (Exception e) {
            log.error("Error sending urgent confirmation (CASH) - Appointment {}: {}",
                    appointment.getId_appointment(), e.getMessage());
        }
    }

    /**
     * 5hs final reminder — MercadoPago variant.
     */
    public void sendFinalConfirmationRemindingWithMercadoPago(Appointment appointment, Pay pay) {
        try {

            String email =  appointment.getPatient().getEmail();

            patientService.validateEmailPatient( email );

            Map<String, Object> data = buildBaseEmailData(appointment);

            addPaymentData(data, pay);

            if (pay.getMercado_pago_data() != null) addMercadoPagoData(data, pay.getMercado_pago_data());

            sendEmail(email, "Tu turno es hoy - Recordatorio final",
                    "email/final-reminder-mercadopago", data);

            log.info("Final reminder (MERCADO_PAGO) sent to: {}", email);

        } catch (Exception e) {
            log.error("Error sending final reminder (MERCADO_PAGO) - Appointment {}: {}",
                    appointment.getId_appointment(), e.getMessage());
        }
    }

    /**
     * 5hs final reminder — Cash variant.
     */
    public void sendFinalConfirmationReminder(Appointment appointment) {
        try {

            String email =  appointment.getPatient().getEmail();

            patientService.validateEmailPatient( email );

            Pay pay = appointment.getPrimaryPayment();

            Map<String, Object> data = buildBaseEmailData(appointment);

            addPaymentData(data, pay);

            sendEmail(email, "Tu turno es hoy - Recordatorio final",
                    "email/final-reminder-cash", data);

            log.info("Final reminder (CASH) sent to: {}", email);

        } catch (Exception e) {
            log.error("Error sending final reminder (CASH) - Appointment {}: {}",
                    appointment.getId_appointment(), e.getMessage());
        }
    }

    /**
     * 5hs final reminder — already confirmed variant (no confirmation button, no payment link).
     */
    public void sendSimpleFinalConfirmationReminder(Appointment appointment) {
        try {

            String email = appointment.getPatient().getEmail();

            patientService.validateEmailPatient(email);

            Map<String, Object> data = buildBaseEmailData(appointment);

            sendEmail(email, "Tu turno es hoy - Recordatorio final",
                    "email/final-reminder-confirmed", data);

            log.info("Simple final reminder sent to: {}", email);

        } catch (Exception e) {
            log.error("Error sending simple final reminder - Appointment {}: {}",
                    appointment.getId_appointment(), e.getMessage());
        }
    }
    // =========================================================================
    // RECEIPTS — policy: propagate exception (they are critical)
    // =========================================================================

    /**
     * Orchestrates all emails + notifications after a CASH payment is confirmed.
     * Policy: critical — exceptions propagate.
     */
    public void sendPaymentReceipt(Appointment appointment, Pay pay, Receipt receipt) {
        try {

            List<Notification> notifications = Arrays.stream( PaymentNotificationConfig.values() )
                    .map(c -> notificationService.buildNotification( appointment, c.getType(), c.getChannel() ) )
                    .collect(Collectors.toList());

            notificationService.saveAll(notifications);

            // Patient: receipt + confirmation (CC dentist on receipt)
            sendCashReceiptToThePatient(appointment, pay, receipt);

            sendAppointmentConfirmationToThePatient(appointment);

            // Dentist: confirmation
            sendAppointmentConfirmationToTheDentist(appointment);

        } catch (Exception e) {
            log.error("Error sending cash payment receipts - Appointment {}: {}", appointment.getId_appointment(), e.getMessage());
            throw new RuntimeException("Failed to send cash payment receipts", e);
        }
    }

    /**
     * Orchestrates all emails + notifications after a MERCADO PAGO payment is confirmed.
     * Policy: critical — exceptions propagate.
     */
    public void sendPaymentReceiptMercadoPago(Appointment appointment, Pay pay,
                                              MercadoPagoPayment payMP, Receipt receipt) {
        try {

            List<Notification> notifications = Arrays.stream(PaymentNotificationConfig.values())
                    .map(c -> notificationService.buildNotification(appointment, c.getType(), c.getChannel() ) )
                    .collect(Collectors.toList());

            notificationService.saveAll(notifications);

            // Patient: receipt + confirmation (CC dentist on receipt)
            sendMercadoPagoReceiptToThePatient(appointment, pay, payMP, receipt);

            sendAppointmentConfirmationToThePatient(appointment);

            // Dentist: confirmation
            sendAppointmentConfirmationToTheDentist(appointment);

        } catch (Exception e) {
            log.error("Error sending MP payment receipts - Appointment {}: {}",
                    appointment.getId_appointment(), e.getMessage());
            throw new RuntimeException("Failed to send MP payment receipts", e);
        }
    }

    /**
     * Cash receipt → patient (PDF attached, CC to dentist).
     */
    public void sendCashReceiptToThePatient(Appointment appointment, Pay pay, Receipt receipt) {
        if (!emailEnabled) return;

        Map<String, Object> data = buildBaseEmailData(appointment);
        addPaymentData(data, pay);
        addReceiptData(data, receipt);
        data.put("isPatient", true);
        data.put("paymentReceivedAt", "Consultorio");

        String patientEmail = appointment.getPatient().getEmail();
        String dentistEmail = extractDentistEmail(appointment);     // CC

        sendEmailWithAttachment(patientEmail,
                                List.of(dentistEmail),                              // CC to dentist
                                "Comprobante de Pago en Efectivo - " + extractClinicName(appointment),
                                "email/cash-payment-receipt",
                                data,
                                receipt
        );

        log.info("Cash receipt sent to patient: {} (CC: {})", patientEmail, dentistEmail);
    }

    /**
     * MercadoPago receipt → patient (PDF attached, CC to dentist).
     */
    public void sendMercadoPagoReceiptToThePatient(Appointment appointment, Pay pay,
                                                   MercadoPagoPayment payMP, Receipt receipt) {
        if (!emailEnabled) return;

        Map<String, Object> data = buildBaseEmailData(appointment);
        addPaymentData(data, pay);
        addMercadoPagoData(data, payMP);
        addReceiptData(data, receipt);
        data.put("isPatient", true);

        String patientEmail = appointment.getPatient().getEmail();
        String dentistEmail = extractDentistEmail(appointment);     // CC

        sendEmailWithAttachment(
                patientEmail,
                List.of(dentistEmail),                              // CC to dentist
                "✅ Comprobante de Pago - " + extractClinicName(appointment),
                "email/mp-payment-receipt",
                data,
                receipt
        );

        log.info("MP receipt sent to patient: {} (CC: {})", patientEmail, dentistEmail);
    }

    /**
     * Appointment confirmation → patient.
     */
    public void sendAppointmentConfirmationToThePatient(Appointment appointment) {
        if (!emailEnabled) return;

        Map<String, Object> data = buildBaseEmailData(appointment);
        data.put("isPatient", true);

        String email = appointment.getPatient().getEmail();

        sendEmailWithAttachment(
                email,
                Collections.emptyList(),
                "🗓️ Turno Confirmado - " + extractClinicName(appointment),
                "appointment-confirmation-patient",
                data,
                null   // sin PDF
        );

        log.info("Appointment confirmation sent to patient: {}", email);
    }

    /**
     * Appointment confirmation → dentist.
     */
    public void sendAppointmentConfirmationToTheDentist(Appointment appointment) {
        if (!emailEnabled) return;

        Map<String, Object> data = buildBaseEmailData(appointment);
        data.put("isPatient", false);

        String email = extractDentistEmail(appointment);

        sendEmailWithAttachment(
                email,
                Collections.emptyList(),
                "📅 Turno Confirmado - Paciente: " + extractPatientFullName(appointment),
                "appointment-confirmation-dentist",
                data,
                null   // sin PDF
        );

        log.info("Appointment confirmation sent to dentist: {}", email);
    }

    // =========================================================================
    // NO-SHOW WARNINGS
    // =========================================================================

    public void sendFirstNoShowWarning(Appointment appointment) {

        trySendToPatient(appointment,
                        "Recordatorio importante sobre tu turno perdido",
                        "email/first-no-show-warning",
                         Collections.emptyMap());
    }

    public void sendSecondNoShowWarning(Appointment appointment) {

        trySendToPatient(appointment,
                        "Segundo turno perdido - Por favor contáctanos",
                        "email/second-no-show-warning",
                         Collections.emptyMap());
    }

    public void sendThirdNoShowWarning(Appointment appointment) {

        trySendToPatient(appointment,
                        "Acción requerida - Múltiples turnos perdidos",
                        "email/third-no-show-warning",
                         Collections.emptyMap());
    }

    /**
     * Helper reutilizable para envíos simples al paciente con política silenciosa.
     */
    private void trySendToPatient(Appointment appointment, String subject,
                                  String template, Map<String, Object> extra) {
        try {
            String email = appointment.getPatient().getEmail();

            patientService.validateEmailPatient(email);

            Map<String, Object> data = buildBaseEmailData(appointment);

            data.putAll(extra);

            sendEmail(email, subject, template, data);

            log.info("Email '{}' sent to: {}", subject, email);

        } catch (Exception e) {
            log.error("Error sending '{}' - Appointment {}: {}",
                    subject, appointment.getId_appointment(), e.getMessage());
        }
    }

    // =========================================================================
    // PUBLIC UTILITIES
    // =========================================================================

    public String buildConfirmationUrl(Notification notification) {
        return String.format("%s/public/appointments/confirm/%d/%s",
                baseUrl,
                notification.getAppointment().getId_appointment(),
                notification.getConfirmation_token());
    }

    // =========================================================================
    // SENDING INFRASTRUCTURE
    // =========================================================================

    /**
     * Simple sending without attachments.
     */
    private void sendEmail(String to, String subject, String templateName, Map<String, Object> model) {
        sendEmailWithAttachment(to, Collections.emptyList(), subject, templateName, model, null);
    }

    /**
     * Unified submission — with or without attached PDF, with or without CC.
     *
     * @param to primary recipient

     * @param ccList CC list (can be empty)

     * @param subject subject

     * @param templateName name of the Thymeleaf template

     * @param model template variables

     * @param receipt if null, no PDF attached
     */
    private void sendEmailWithAttachment(String to, List<String> ccList, String subject,
                                         String templateName, Map<String, Object> model,
                                         Receipt receipt) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);

            // CC
            if ( ccList != null && !ccList.isEmpty() ) {

                List<String> validCCs = ccList.stream().filter(email -> email != null && !email.isBlank() )
                                                       .toList();

                if (!validCCs.isEmpty()) {
                    helper.setCc( validCCs.toArray( new String[0] ));
                }
            }

            Context context = new Context( Locale.forLanguageTag("es-AR") );

            context.setVariables(model);

            helper.setText( templateEngine.process(templateName, context), true);

            // PDF (optional)
            if (receipt != null) {

                byte[] pdfBytes = downloadPdfFromUrl(receipt.getUrl_pdf());

                helper.addAttachment( receipt.getFilename(), new ByteArrayResource(pdfBytes), "application/pdf");
            }

            mailSender.send(message);
            log.debug("Email sent: '{}' → {}", subject, to);

        } catch (MessagingException | RuntimeException e) {

            log.error("Error sending email '{}' to {}: {}", subject, to, e.getMessage());

            throw new RuntimeException("Error sending email: " + subject, e);
        } catch (Exception e) {

            log.error("Unexpected error sending email '{}' to {}: {}", subject, to, e.getMessage());

            throw new RuntimeException("Unexpected error sending email: " + subject, e);
        }
    }

    private byte[] downloadPdfFromUrl(String pdfUrl) throws Exception {
        try (InputStream in = new URL(pdfUrl).openStream()) {
            return in.readAllBytes();
        }
    }

    // =========================================================================
    // DATA BUILDERS
    // =========================================================================

    /**
     * Base data for ALL emails
     */
    private Map<String, Object> buildBaseEmailData(Appointment appointment) {

        UserProfile dentistProfile = appointment.getDentist().getUserProfile();

        Map<String, Object> data = new HashMap<>();

        // Clinic
        data.put("clinicName",      dentistProfile.getClinic().getName());
        data.put("clinicLogoUrl",   clinicLogoUrl);
        data.put("clinicEmail",     dentistProfile.getClinic().getEmail());
        data.put("clinicaWhatsApp", dentistProfile.getClinic().getPhone_number());

        // Patient
        data.put("paciente",      appointment.getPatient().getName());
        data.put("patientName",   extractPatientFullName(appointment));
        data.put("patientEmail",  appointment.getPatient().getEmail());
        data.put("patientPhone",  appointment.getPatient().getPhone_number());
        data.put("patientDni",    appointment.getPatient().getDni());

        // Dentist
        data.put("dentista",      dentistProfile.getName());
        data.put("dentistName",   dentistProfile.getName() + " " + dentistProfile.getSurname());
        data.put("dentistEmail",  dentistProfile.getAuthUser().getUsername());
        data.put("dentistPhone",  dentistProfile.getPhone_number());

        // Appointment
        data.put("appointmentId",   appointment.getId_appointment());
        data.put("fecha",           appointment.getDate().format(DATE_FORMATTER));
        data.put("hora",            appointment.getStartTime().format(TIME_FORMATTER));
        data.put("appointmentDate", appointment.getDate().format(DATE_FORMATTER));
        data.put("appointmentTime", appointment.getStartTime().format(TIME_FORMATTER));
        data.put("duration",        appointment.getDuration_minutes() + " minutos");

        // Treatment
        String treatmentName = (appointment.getTreatment() != null
                && appointment.getTreatment().getProduct() != null)
                ? appointment.getTreatment().getProduct().getName_product()
                : "Consulta general";
        data.put("treatmentName", treatmentName);

        // Instructions
        data.put("patientInstructions", appointment.getPatient_instructions() != null
                ? appointment.getPatient_instructions()
                : "Llegar 20 minutos antes de la hora del turno.");

        // Status
        data.put("isConfirmed", appointment.getAttendanceConfirmed());
        data.put("status", "CONFIRMADO");

        // Footer
        data.put("currentYear", java.time.Year.now().getValue());
        data.put("baseUrl", baseUrl);

        return data;
    }

    private void addPaymentData(Map<String, Object> data, Pay pay) {
        data.put("monto",         pay.getAmount());
        data.put("paymentAmount", String.format("$%.2f", pay.getAmount()));
        data.put("paymentMethod", translatePaymentMethod(pay.getPayment_method().name()));
        data.put("paymentStatus", pay.getPayment_status() == PaymentStatus.PAID ? "Aprobado" : "Pendiente");
        data.put("isPaid",        pay.getPayment_status() == PaymentStatus.PAID);
    }

    private void addMercadoPagoData(Map<String, Object> data, MercadoPagoPayment payMP) {

        if (payMP == null) return;

        data.put("mpPaymentId",        payMP.getPaymentId() != null ? payMP.getPaymentId() : "N/A");
        data.put("mpExternalReference", payMP.getExternalReference());
        data.put("mpDateApproved",     payMP.getDateApproved() != null ? payMP.getDateApproved().format(DATETIME_FORMATTER) : "N/A");
        data.put("mpInstallments",     payMP.getInstallments() != null && payMP.getInstallments() > 1 ? payMP.getInstallments() + " cuotas" : "Pago único");
        data.put("paymentLink",        payMP.getInitPoint());
    }

    private void addReceiptData(Map<String, Object> data, Receipt receipt) {
        data.put("receiptNumber", receipt.getFilename());
    }

    // =========================================================================
    // PRIVATE HELPERS
    // =========================================================================

    private String extractDentistEmail(Appointment appointment) {
        return appointment.getDentist().getUserProfile().getAuthUser().getUsername();
    }

    private String extractClinicName(Appointment appointment) {
        return appointment.getDentist().getUserProfile().getClinic().getName();
    }

    private String extractPatientFullName(Appointment appointment) {
        return appointment.getPatient().getName() + " " + appointment.getPatient().getSurname();
    }

    private String translatePaymentMethod(String method) {
        return switch (method) {
            case "CASH"          -> "Efectivo";
            case "CREDIT_CARD"   -> "Tarjeta de Crédito";
            case "DEBIT_CARD"    -> "Tarjeta de Débito";
            case "MERCADO_PAGO"  -> "Mercado Pago";
            default              -> method;
        };
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

}