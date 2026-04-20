package com.dentify.integration.email.listener;

import com.dentify.domain.receipt.model.Receipt;
import com.dentify.domain.receipt.service.IReceiptService;
import com.dentify.integration.email.event.AppointmentRegisteredEvent;
import com.dentify.integration.email.event.CashPaymentCompletedEvent;
import com.dentify.integration.email.event.MercadoPagoAppointmentRegisteredEvent;
import com.dentify.integration.email.service.IEmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmailEventListener {

    private final IEmailService emailService;
    private final IReceiptService receiptService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onCashPaymentCompleted( CashPaymentCompletedEvent event) {

        try {
            Receipt receipt = receiptService.generateAndSaveReceipt( event.payment(), event.appointment(), null);

            emailService.sendCashPaymentReceipt( event.appointment(), event.payment(), receipt);
        } catch (Exception e) {
            log.error("Error sending cash payment receipt email — appointment {}: {}", event.appointment().getId_appointment(), e.getMessage());
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onNewAppointmentRegistered( AppointmentRegisteredEvent event) {

        try {
            emailService.sendFirstConfirmations( event.appointment() );
        } catch (Exception e) {
            log.error("Error sending appointment confirmation email — appointment {}: {}", event.appointment().getId_appointment(), e.getMessage());
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onMercadoPagoAppointmentRegistered(MercadoPagoAppointmentRegisteredEvent event) {
        try {
            emailService.sendFirstConfirmationsMercadoPago(event.appointment(), event.initPoint());
        } catch (Exception e) {
            log.error("Error sending MP confirmation — appointment {}: {}",
                    event.appointment().getId_appointment(), e.getMessage());
        }
    }
}