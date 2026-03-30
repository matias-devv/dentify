package com.dentify.integration.email.service;


import com.dentify.domain.appointment.model.Appointment;
import com.dentify.domain.dentist.service.IDentistService;
import com.dentify.domain.invitation.enums.InvitedRole;
import com.dentify.domain.invitation.model.Invitation;
import com.dentify.domain.mercadopagopayment.model.MercadoPagoPayment;
import com.dentify.domain.notification.enums.PaymentNotificationConfig;
import com.dentify.domain.notification.model.Notification;
import com.dentify.domain.notification.service.INotificationService;
import com.dentify.domain.patient.model.Patient;
import com.dentify.domain.patient.service.IPatientService;
import com.dentify.domain.pay.enums.PaymentMethod;
import com.dentify.domain.pay.enums.PaymentStatus;
import com.dentify.domain.pay.model.Pay;
import com.dentify.domain.receipt.model.Receipt;
import com.dentify.domain.userProfile.model.UserProfile;
import com.dentify.security.model.AuthUser;
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

@Slf4j
@RequiredArgsConstructor
public class EmailService implements IEmailService {

    // =========================================================================
    // CONFIGURATION
    // =========================================================================

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${notifications.email.enabled:true}")
    private boolean emailEnabled;

    @Value("${app.base-url}")
    private String baseUrl;

    @Value("${clinic.logo.url:}")
    private String clinicLogoUrl;

    // =========================================================================
    // DEPENDENCIES
    // =========================================================================

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final INotificationService notificationService;
    private final IPatientService patientService;
    private final IDentistService dentistService;

    // =========================================================================
    // CONSTANTS — FORMATTERS
    // =========================================================================

    private static final DateTimeFormatter DATE_FORMATTER     = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FORMATTER     = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // =========================================================================
    // CONSTANTS — TEMPLATE NAMES
    // =========================================================================

    private static final String TPL_DENTIFY_INVITATION            = "email/dentify-invitation";
    private static final String TPL_DENTIST_INVITATION            = "email/dentist-invitation";
    private static final String TPL_SECRETARY_INVITATION          = "email/secretary-invitation";
    private static final String TPL_APPT_CANCELLED_MANUAL_PATIENT = "email/appointment-cancelled-manual-patient";
    private static final String TPL_APPT_CANCELLED_MANUAL_DENTIST = "email/appointment-cancelled-manual-dentist";
    private static final String TPL_APPT_CANCELLED_BY_SYSTEM      = "email/appointment-cancelled-by-system";
    private static final String TPL_CONFIRMATION_MP_48H           = "email/confirmation-request-mercadopago-48h";
    private static final String TPL_CONFIRMATION_CASH_48H         = "email/confirmation-request-cash-48h";
    private static final String TPL_URGENT_CONFIRMATION_MP_24H    = "email/urgent-confirmation-request-mercadopago-24h";
    private static final String TPL_URGENT_CONFIRMATION_CASH_24H  = "email/urgent-confirmation-request-cash-24h";
    private static final String TPL_FINAL_REMINDER_MP             = "email/final-reminder-mercadopago";
    private static final String TPL_FINAL_REMINDER_CASH           = "email/final-reminder-cash";
    private static final String TPL_FINAL_REMINDER_CONFIRMED      = "email/final-reminder-confirmed";
    private static final String TPL_CASH_RECEIPT                  = "email/cash-payment-receipt";
    private static final String TPL_MP_RECEIPT                    = "email/mp-payment-receipt";
    private static final String TPL_APPT_CONFIRMATION_PATIENT     = "appointment-confirmation-patient";
    private static final String TPL_APPT_CONFIRMATION_DENTIST     = "appointment-confirmation-dentist";
    private static final String TPL_NO_SHOW_FIRST                 = "email/first-no-show-warning";
    private static final String TPL_NO_SHOW_SECOND                = "email/second-no-show-warning";
    private static final String TPL_NO_SHOW_THIRD                 = "email/third-no-show-warning";

    // =========================================================================
    // CONSTANTS — EMAIL SUBJECTS
    // =========================================================================

    private static final String SUBJECT_APPT_CANCELLED             = "Turno cancelado - ";
    private static final String SUBJECT_APPT_CANCELLED_BY_SYSTEM   = "Turno cancelado por falta de confirmacion";
    private static final String SUBJECT_CONFIRMATION_48H           = "Confirmá tu asistencia - Turno en 2 días";
    private static final String SUBJECT_URGENT_CONFIRMATION_24H    = "URGENTE - Confirmá tu turno o se cancelará";
    private static final String SUBJECT_FINAL_REMINDER             = "Tu turno es hoy - Recordatorio final";
    private static final String SUBJECT_CASH_RECEIPT               = "Comprobante de Pago en Efectivo - ";
    private static final String SUBJECT_MP_RECEIPT                 = "✅ Comprobante de Pago - ";
    private static final String SUBJECT_APPT_CONFIRMED_PATIENT     = "🗓️ Turno Confirmado - ";
    private static final String SUBJECT_APPT_CONFIRMED_DENTIST     = "📅 Turno Confirmado - Paciente: ";
    private static final String SUBJECT_RECEIPT_ISSUED_DENTIST     = "Comprobante emitido - ";
    private static final String SUBJECT_MP_RECEIPT_ISSUED_DENTIST  = "Comprobante MP emitido - ";
    private static final String SUBJECT_NO_SHOW_FIRST              = "Recordatorio importante sobre tu turno perdido";
    private static final String SUBJECT_NO_SHOW_SECOND             = "Segundo turno perdido - Por favor contáctanos";
    private static final String SUBJECT_NO_SHOW_THIRD              = "Acción requerida - Múltiples turnos perdidos";

    // =========================================================================
    // INVITATIONS
    // =========================================================================

    @Override
    public void sendInvitation(Invitation invitation, UserProfile inviter, boolean isAdmin) {
        if (!emailEnabled) return;

        if (isAdmin) {
            sendAdminInvitation(invitation, inviter.getAuthUser());
        } else {
            sendDentistOrSecretaryInvitation(invitation, inviter);
        }
    }

    private void sendAdminInvitation(Invitation invitation, AuthUser authUser) {

        String email = invitation.getEmail();

        if ( isBlank(email) ) {
            log.warn("Admin invitation without email — id: {}", invitation.getId());
            return;
        }
        try {

            Map<String, Object> data = new HashMap<>();
            data.put("invitedEmail",    email);
            data.put("registrationUrl", buildDentistRegistrationUrl(invitation.getToken()));
            addFooterData(data);

            sendEmail(email, "Tu acceso a Dentify está listo", TPL_DENTIFY_INVITATION, data);
            log.info("Admin invitation sent to: {}", email);
        } catch (Exception e) {
            log.error("Error sending admin invitation — id {}: {}", invitation.getId(), e.getMessage());
        }
    }

    private void sendDentistOrSecretaryInvitation(Invitation invitation, UserProfile inviter) {
        String email = invitation.getEmail();

        if (isBlank(email)) {
            log.warn("Invitation without email — id: {}", invitation.getId());
            return;
        }
        try {
            boolean isDentist = invitation.getInvitedRole() == InvitedRole.DENTIST;

            Map<String, Object> data = new HashMap<>();
            data.put("clinicName",    inviter.getClinic().getName());
            data.put("clinicEmail",   inviter.getClinic().getEmail());
            data.put("inviterName",   inviter.getName() + " " + inviter.getSurname());
            data.put("invitedRole",   isDentist ? "Odontólogo/a" : "Secretario/a");
            data.put("invitedEmail",  email);
            addFooterData(data);

            if (isDentist) {
                data.put("registrationUrl", buildDentistRegistrationUrl(invitation.getToken()));
                sendEmail(email, "Te invitaron a unirte a " + inviter.getClinic().getName(), TPL_DENTIST_INVITATION, data);
            } else {
                data.put("registrationUrl", buildSecretaryRegistrationUrl(invitation.getToken()));
                sendEmail(email, "Te invitaron a unirte a " + inviter.getClinic().getName(), TPL_SECRETARY_INVITATION, data);
            }
            log.info("Invitation sent to {} — role: {}", email, invitation.getInvitedRole());
        } catch (Exception e) {
            log.error("Error sending invitation — id {}: {}", invitation.getId(), e.getMessage());
        }
    }

    // =========================================================================
    // CANCELLATIONS
    // =========================================================================

    @Override
    public void sendAppointmentManuallyCancelled(Appointment appointment) {
        if (!emailEnabled) return;

        String reason     = appointment.getReason_for_cancellation() != null
                                                                            ? appointment.getReason_for_cancellation()
                                                                            : "El turno fue cancelado por la clínica.";

        String cancelledBy = appointment.getAppointmentStatus().name();

        notifyPatientOfManualCancellation(appointment, reason, cancelledBy);
        notifyDentistOfManualCancellation(appointment, reason, cancelledBy);
    }

    private void notifyPatientOfManualCancellation(Appointment appointment, String reason, String cancelledBy) {
        try {
            List<String> emails = resolveAndValidatePatientEmails(appointment.getPatient());

            Map<String, Object> data = buildBaseEmailData(appointment);
            data.put("cancellationReason", reason);
            data.put("cancelledBy",        cancelledBy);
            data.put("isPatient",          true);

            sendToPatientEmails(emails, appointment.getPatient(),
                               SUBJECT_APPT_CANCELLED + extractClinicName(appointment),
                                TPL_APPT_CANCELLED_MANUAL_PATIENT, data, null);

            log.info("Manual cancellation sent to {} patient recipient(s) — appointment {}", emails.size(), appointment.getId_appointment());
        } catch (Exception e) {
            log.error("Error sending manual cancellation to patient — appointment {}: {}",
                    appointment.getId_appointment(), e.getMessage());
        }
    }

    private void notifyDentistOfManualCancellation(Appointment appointment, String reason, String cancelledBy) {
        try {
            String email = extractDentistEmail(appointment);
            dentistService.validateDentistEmail(email);

            Map<String, Object> data = buildBaseEmailData(appointment);
            data.put("cancellationReason", reason);
            data.put("cancelledBy",        cancelledBy);
            data.put("isPatient",          false);

            sendEmail(email,
                     SUBJECT_APPT_CANCELLED + "Paciente: " + extractPatientFullName(appointment),
                      TPL_APPT_CANCELLED_MANUAL_DENTIST, data);

            log.info("Manual cancellation sent to dentist — appointment {}", appointment.getId_appointment());
        } catch (Exception e) {
            log.error("Error sending manual cancellation to dentist — appointment {}: {}", appointment.getId_appointment(), e.getMessage());
        }
    }

    @Override
    public void sendAppointmentCancelledBySystem(Appointment appointment) {
        if (!emailEnabled) return;
        try {
            List<String> emails = resolveAndValidatePatientEmails(appointment.getPatient());

            Map<String, Object> data = buildBaseEmailData(appointment);
            data.put("cancellationReason", "Turno cancelado por falta de confirmacion.");

            sendToPatientEmails(emails, appointment.getPatient(), SUBJECT_APPT_CANCELLED_BY_SYSTEM, TPL_APPT_CANCELLED_BY_SYSTEM, data, null);

            log.info("System cancellation sent to {} recipient(s) — appointment {}", emails.size(), appointment.getId_appointment());
        } catch (Exception e) {
            log.error("Error sending system cancellation — appointment {}: {}",
                    appointment.getId_appointment(), e.getMessage());
        }
    }

    // =========================================================================
    // CONFIRMATIONS AND REMINDERS
    // =========================================================================

    @Override
    public void sendConfirmationRequest(Notification notification) {

        Appointment appointment = notification.getAppointment();

        if (appointment.getPrimaryPaymentMethod() == PaymentMethod.MERCADO_PAGO) {
            sendConfirmationWithPaymentOption(notification, appointment, false);
        } else {
            sendConfirmationCashOnly(notification, appointment, false);
        }
    }

    @Override
    public void urgentConfirmationRequest(Notification notification) {

        Appointment appointment = notification.getAppointment();

        if (appointment.getPrimaryPaymentMethod() == PaymentMethod.MERCADO_PAGO) {
            sendConfirmationWithPaymentOption(notification, appointment, true);
        } else {
            sendConfirmationCashOnly(notification, appointment, true);
        }
    }

    /**
     * Shared logic for 48h and 24h MP confirmation requests.
     *
     * @param urgent true → 24h subject/template; false → 48h
     */
    private void sendConfirmationWithPaymentOption(Notification notification, Appointment appointment, boolean urgent) {
        try {
            List<String> emails = resolveAndValidatePatientEmails(appointment.getPatient());

            Pay pay = appointment.getPrimaryPayment();

            Map<String, Object> data = buildBaseEmailData(appointment);

            addPaymentData(data, pay);

            data.put("confirmationUrl", buildConfirmationUrl(notification));

            if (pay.getMercado_pago_data() != null) addMercadoPagoData(data, pay.getMercado_pago_data());

            String subject  = urgent ? SUBJECT_URGENT_CONFIRMATION_24H : SUBJECT_CONFIRMATION_48H;
            String template = urgent ? TPL_URGENT_CONFIRMATION_MP_24H  : TPL_CONFIRMATION_MP_48H;

            sendToPatientEmails(emails, appointment.getPatient(), subject, template, data, null);

            log.info("Confirmation (MP, urgent={}) sent to {} recipient(s)", urgent, emails.size());
        } catch (Exception e) {
            log.error("Error sending MP confirmation (urgent={}) — appointment {}: {}",
                    urgent, appointment.getId_appointment(), e.getMessage());
        }
    }

    /**
     * Shared logic for 48h and 24h Cash confirmation requests.
     *
     * @param urgent true → 24h subject/template; false → 48h
     */
    private void sendConfirmationCashOnly(Notification notification, Appointment appointment, boolean urgent) {
        try {

            List<String> emails = resolveAndValidatePatientEmails(appointment.getPatient());

            Pay pay = appointment.getPrimaryPayment();

            Map<String, Object> data = buildBaseEmailData(appointment);

            addPaymentData(data, pay);

            data.put("confirmationUrl", buildConfirmationUrl(notification));

            String subject  = urgent ? SUBJECT_URGENT_CONFIRMATION_24H : SUBJECT_CONFIRMATION_48H;
            String template = urgent ? TPL_URGENT_CONFIRMATION_CASH_24H : TPL_CONFIRMATION_CASH_48H;

            sendToPatientEmails( emails, appointment.getPatient(), subject, template, data, null);

            log.info("Confirmation (CASH, urgent={}) sent to {} recipient(s)", urgent, emails.size());
        } catch (Exception e) {
            log.error("Error sending CASH confirmation (urgent={}) — appointment {}: {}",
                    urgent, appointment.getId_appointment(), e.getMessage());
        }
    }

    @Override
    public void sendFinalConfirmationRemindingWithMercadoPago(Appointment appointment, Pay pay) {
        try {

            List<String> emails = resolveAndValidatePatientEmails(appointment.getPatient());

            Map<String, Object> data = buildBaseEmailData(appointment);

            addPaymentData(data, pay);

            if (pay.getMercado_pago_data() != null) addMercadoPagoData(data, pay.getMercado_pago_data());

            sendToPatientEmails( emails, appointment.getPatient(), SUBJECT_FINAL_REMINDER, TPL_FINAL_REMINDER_MP, data, null);

            log.info("Final reminder (MP) sent to {} recipient(s)", emails.size());
        } catch (Exception e) {
            log.error("Error sending final reminder (MP) — appointment {}: {}",
                    appointment.getId_appointment(), e.getMessage());
        }
    }

    @Override
    public void sendFinalConfirmationReminder(Appointment appointment) {
        try {

            List<String> emails = resolveAndValidatePatientEmails(appointment.getPatient());

            Pay pay = appointment.getPrimaryPayment();

            Map<String, Object> data = buildBaseEmailData(appointment);

            addPaymentData(data, pay);

            sendToPatientEmails( emails, appointment.getPatient(), SUBJECT_FINAL_REMINDER, TPL_FINAL_REMINDER_CASH, data, null);

            log.info("Final reminder (CASH) sent to {} recipient(s)", emails.size());
        } catch (Exception e) {
            log.error("Error sending final reminder (CASH) — appointment {}: {}",
                    appointment.getId_appointment(), e.getMessage());
        }
    }

    @Override
    public void sendSimpleFinalConfirmationReminder(Appointment appointment) {
        try {
            List<String> emails = resolveAndValidatePatientEmails(appointment.getPatient());

            Map<String, Object> data = buildBaseEmailData(appointment);

            sendToPatientEmails(emails, appointment.getPatient(), SUBJECT_FINAL_REMINDER, TPL_FINAL_REMINDER_CONFIRMED, data, null);

            log.info("Simple final reminder sent to {} recipient(s)", emails.size());
        } catch (Exception e) {
            log.error("Error sending simple final reminder — appointment {}: {}",
                    appointment.getId_appointment(), e.getMessage());
        }
    }

    // =========================================================================
    // RECEIPTS
    // =========================================================================

    @Override
    public void sendCashPaymentReceipt(Appointment appointment, Pay pay, Receipt receipt) {
        try {
            createAndSavePaymentNotifications(appointment);

            List<String> emails = resolveAndValidatePatientEmails(appointment.getPatient());

            dispatchCashReceipt(appointment, pay, receipt, emails);

            sendAppointmentConfirmationToPatientInternal(appointment, emails);

            sendAppointmentConfirmationToDentist(appointment);

        } catch (Exception e) {
            log.error("Error sending cash payment receipts — appointment {}: {}", appointment.getId_appointment(), e.getMessage());
            throw new RuntimeException("Failed to send cash payment receipts", e);
        }
    }

    @Override
    public void sendCashReceiptToThePatient(Appointment appointment, Pay pay, Receipt receipt) {

        if (!emailEnabled) return;
        try {

            List<String> emails = resolveAndValidatePatientEmails(appointment.getPatient());

            dispatchCashReceipt(appointment, pay, receipt, emails);
        } catch (Exception e) {
            log.error("Error sending cash receipt to patient — appointment {}: {}", appointment.getId_appointment(), e.getMessage());
        }
    }

    @Override
    public void sendPaymentReceiptMercadoPago(Appointment appointment, Pay pay,
                                              MercadoPagoPayment payMP, Receipt receipt) {
        try {
            createAndSavePaymentNotifications(appointment);

            List<String> emails = resolveAndValidatePatientEmails(appointment.getPatient());

            dispatchMercadoPagoReceipt(appointment, pay, payMP, receipt, emails);

            sendAppointmentConfirmationToPatientInternal(appointment, emails);

            sendAppointmentConfirmationToDentist(appointment);

        } catch (Exception e) {
            log.error("Error sending MP payment receipts — appointment {}: {}",
                    appointment.getId_appointment(), e.getMessage());
            throw new RuntimeException("Failed to send MP payment receipts", e);
        }
    }

    /**
     * Sends the cash PDF receipt to each patient recipient and a copy to the dentist.
     * Extracted to avoid duplication between {@code sendCashPaymentReceipt} and
     * {@code sendCashReceiptToThePatient}.
     */
    private void dispatchCashReceipt(Appointment appointment, Pay pay, Receipt receipt, List<String> patientEmails) {

        Map<String, Object> data = buildBaseEmailData(appointment);

        addPaymentData(data, pay);

        addReceiptData(data, receipt);

        data.put("isPatient", true);
        data.put("paymentReceivedAt", "Consultorio");

        String subject  = SUBJECT_CASH_RECEIPT + extractClinicName(appointment);

        sendToPatientEmails(patientEmails, appointment.getPatient(), subject, TPL_CASH_RECEIPT, data, receipt);

        Map<String, Object> dentistData = buildDentistCopy(data);

        sendEmail( this.extractDentistEmail(appointment),
                 SUBJECT_RECEIPT_ISSUED_DENTIST + extractPatientFullName(appointment),
                  TPL_CASH_RECEIPT, dentistData, receipt);

        log.info("Cash receipt dispatched to {} patient recipient(s) + dentist copy", patientEmails.size());
    }

    /**
     * Sends the MercadoPago PDF receipt to each patient recipient and a copy to the dentist.
     */
    private void dispatchMercadoPagoReceipt(Appointment appointment, Pay pay, MercadoPagoPayment payMP, Receipt receipt, List<String> patientEmails) {

        Map<String, Object> data = buildBaseEmailData(appointment);

        addPaymentData(data, pay);

        addMercadoPagoData(data, payMP);

        addReceiptData(data, receipt);

        data.put("isPatient", true);

        String subject = SUBJECT_MP_RECEIPT + extractClinicName(appointment);

        sendToPatientEmails(patientEmails, appointment.getPatient(), subject, TPL_MP_RECEIPT, data, receipt);

        Map<String, Object> dentistData = buildDentistCopy(data);

        sendEmail( this.extractDentistEmail(appointment),
                  SUBJECT_MP_RECEIPT_ISSUED_DENTIST + extractPatientFullName(appointment),
                  TPL_MP_RECEIPT, dentistData, receipt);

        log.info("MP receipt dispatched to {} patient recipient(s) + dentist copy", patientEmails.size());
    }

    /**
     * Sends the appointment confirmation email to the patient after a payment is confirmed.
     * This is always called after a receipt is dispatched — not as a standalone action.
     */
    private void sendAppointmentConfirmationToPatientInternal(Appointment appointment, List<String> emails) {
        Map<String, Object> data = buildBaseEmailData(appointment);
        data.put("isPatient", true);

        sendToPatientEmails(emails, appointment.getPatient(),
                SUBJECT_APPT_CONFIRMED_PATIENT + extractClinicName(appointment),
                TPL_APPT_CONFIRMATION_PATIENT, data, null);

        log.info("Appointment confirmation sent to {} patient recipient(s)", emails.size());
    }

    /**
     * Sends the appointment confirmation email to the dentist after a payment is confirmed.
     * Always a single recipient — no PDF attachment.
     */
    private void sendAppointmentConfirmationToDentist(Appointment appointment) {
        Map<String, Object> data = buildBaseEmailData(appointment);
        data.put("isPatient",          false);
        data.put("isResponsibleAdult", false);

        sendEmail(extractDentistEmail(appointment),
                SUBJECT_APPT_CONFIRMED_DENTIST + extractPatientFullName(appointment),
                TPL_APPT_CONFIRMATION_DENTIST, data);

        log.info("Appointment confirmation sent to dentist — appointment {}", appointment.getId_appointment());
    }

    // =========================================================================
    // NO-SHOW WARNINGS
    // =========================================================================

    @Override
    public void sendFirstNoShowWarning(Appointment appointment) {
        trySendToPatient(appointment, SUBJECT_NO_SHOW_FIRST, TPL_NO_SHOW_FIRST, Collections.emptyMap());
    }

    @Override
    public void sendSecondNoShowWarning(Appointment appointment) {
        trySendToPatient(appointment, SUBJECT_NO_SHOW_SECOND, TPL_NO_SHOW_SECOND, Collections.emptyMap());
    }

    @Override
    public void sendThirdNoShowWarning(Appointment appointment) {
        trySendToPatient(appointment, SUBJECT_NO_SHOW_THIRD, TPL_NO_SHOW_THIRD, Collections.emptyMap());
    }

    /**
     * Reusable silent-policy sender for simple patient notifications.
     * Builds the base email data, merges any extra model entries, and sends.
     * Errors are logged but never propagated.
     */
    private void trySendToPatient(Appointment appointment, String subject,
                                  String template, Map<String, Object> extra) {
        try {
            List<String> emails = resolveAndValidatePatientEmails(appointment.getPatient());

            Map<String, Object> data = buildBaseEmailData(appointment);
            data.putAll(extra);

            sendToPatientEmails(emails, appointment.getPatient(), subject, template, data, null);
            log.info("'{}' sent to {} recipient(s)", subject, emails.size());
        } catch (Exception e) {
            log.error("Error sending '{}' — appointment {}: {}",
                    subject, appointment.getId_appointment(), e.getMessage());
        }
    }

    // =========================================================================
    // SENDING INFRASTRUCTURE
    // =========================================================================

    /**
     * Sends a plain email without attachment.
     */
    private void sendEmail(String to, String subject, String templateName, Map<String, Object> model) {
        sendEmail(to, subject, templateName, model, null);
    }

    /**
     * Sends a single email, optionally attaching a PDF receipt.
     * Exceptions propagate to let callers decide the error-handling policy.
     */
    private void sendEmail(String to, String subject, String templateName,
                           Map<String, Object> model, Receipt receipt) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);

            Context context = new Context(Locale.forLanguageTag("es-AR"));
            context.setVariables(model);
            helper.setText(templateEngine.process(templateName, context), true);

            if (receipt != null) {
                byte[] pdfBytes = downloadPdfFromUrl(receipt.getUrl_pdf());
                helper.addAttachment(receipt.getFilename(), new ByteArrayResource(pdfBytes), "application/pdf");
            }

            mailSender.send(message);
            log.debug("Email dispatched — subject: '{}'", subject);

        } catch (MessagingException | RuntimeException e) {
            log.error("Error sending email '{}': {}", subject, e.getMessage());
            throw new RuntimeException("Error sending email: " + subject, e);
        } catch (Exception e) {
            log.error("Unexpected error sending email '{}': {}", subject, e.getMessage());
            throw new RuntimeException("Unexpected error sending email: " + subject, e);
        }
    }

    /**
     * Sends one individual email per patient-side recipient, injecting
     * {@code isResponsibleAdult} into each message's model so templates
     * can personalize the salutation. No recipient sees another's address.
     */
    private void sendToPatientEmails(List<String> emails, Patient patient, String subject, String templateName,
                                     Map<String, Object> baseModel, Receipt receipt) {
        String patientOwnEmail = patient.getEmail();

        for (String email : emails) {

            Map<String, Object> model = new HashMap<>(baseModel);

            model.put("isResponsibleAdult", patientOwnEmail == null || !email.equals(patientOwnEmail));

            sendEmail(email, subject, templateName, model, receipt);
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
     * Builds the common model map used as the base for all email templates.
     * Contains clinic, patient, dentist, appointment, and treatment data.
     * <p>
     * Security note: this map intentionally omits any list of emails so that
     * templates cannot inadvertently expose addresses of other recipients.
     */
    private Map<String, Object> buildBaseEmailData(Appointment appointment) {
        UserProfile dentistProfile = appointment.getDentist().getUserProfile();

        Map<String, Object> data = new HashMap<>();

        // Clinic
        data.put("clinicName",      dentistProfile.getClinic().getName());
        data.put("clinicLogoUrl",   clinicLogoUrl);
        data.put("clinicEmail",     dentistProfile.getClinic().getEmail());
        data.put("clinicaWhatsApp", dentistProfile.getClinic().getPhone_number());

        // Patient — only personal data, never their email list
        data.put("paciente",      appointment.getPatient().getName());
        data.put("patientName",   extractPatientFullName(appointment));
        data.put("patientPhone",  appointment.getPatient().getPhone_number());
        data.put("patientDni",    appointment.getPatient().getDni());

        // Dentist — only name and direct contact; no other user data
        data.put("dentista",     dentistProfile.getName());
        data.put("dentistName",  dentistProfile.getName() + " " + dentistProfile.getSurname());
        data.put("dentistPhone", dentistProfile.getPhone_number());

        // Appointment
        data.put("appointmentId",   appointment.getId_appointment());
        data.put("fecha",           appointment.getAppointmentDate().toLocalDate().format(DATE_FORMATTER));
        data.put("hora",            appointment.getAppointmentDate().toLocalTime().format(TIME_FORMATTER));
        data.put("appointmentDate", appointment.getAppointmentDate().toLocalDate().format(DATE_FORMATTER));
        data.put("appointmentTime", appointment.getAppointmentDate().toLocalTime().format(TIME_FORMATTER));
        data.put("duration",        appointment.getDuration_minutes() + " minutos");

        // Treatment
        String treatmentName = (appointment.getTreatment() != null
                && appointment.getTreatment().getProduct() != null)
                ? appointment.getTreatment().getProduct().getNameProduct()
                : "Consulta general";
        data.put("treatmentName", treatmentName);

        // Instructions
        data.put("patientInstructions", appointment.getPatient_instructions() != null
                ? appointment.getPatient_instructions()
                : "Llegar 20 minutos antes de la hora del turno.");

        // Status
        data.put("isConfirmed", appointment.getAttendanceConfirmed());
        data.put("status",      "CONFIRMADO");

        // Footer
        addFooterData(data);

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

        data.put("mpPaymentId",         payMP.getPaymentId()        != null ? payMP.getPaymentId()    : "N/A");

        data.put("mpExternalReference", payMP.getExternalReference() != null ? payMP.getExternalReference()   : "N/A");

        data.put("mpDateApproved",      payMP.getDateApproved()      != null ? payMP.getDateApproved().format(DATETIME_FORMATTER) : "N/A");

        data.put("mpInstallments",      payMP.getInstallments() != null && payMP.getInstallments() > 1 ? payMP.getInstallments() + " cuotas" : "Pago único");

        data.put("paymentLink",         payMP.getInitPoint());
    }

    private void addReceiptData(Map<String, Object> data, Receipt receipt) {
        data.put("receiptNumber", receipt.getFilename());
    }

    private void addFooterData(Map<String, Object> data) {
        data.put("currentYear", java.time.Year.now().getValue());
        data.put("baseUrl",     baseUrl);
    }

    /**
     * Builds a dentist-copy model from an existing patient data map,
     * overriding recipient-specific flags so dentist templates render correctly.
     */
    private Map<String, Object> buildDentistCopy(Map<String, Object> patientData) {

        Map<String, Object> dentistData = new HashMap<>(patientData);

        dentistData.put("isPatient",          false);

        dentistData.put("isResponsibleAdult", false);

        return dentistData;
    }

    // =========================================================================
    // PRIVATE UTILITIES
    // =========================================================================

    /**
     * Creates and persists all payment-related notifications for a confirmed appointment.
     */
    private void createAndSavePaymentNotifications(Appointment appointment) {

        List<com.dentify.domain.notification.model.Notification> notifications =
                  Arrays.stream(PaymentNotificationConfig.values())
                        .map(c -> notificationService.buildNotification(appointment, c.getType(), c.getChannel()))
                        .collect(Collectors.toList());

        notificationService.saveAll(notifications);
    }

    /**
     * Resolves patient emails (own + responsible adults) and validates that
     * at least one valid address exists before sending.
     *
     * @throws RuntimeException if no valid email is available
     */
    private List<String> resolveAndValidatePatientEmails(Patient patient) {
        List<String> emails = patientService.resolvePatientEmail(patient);

        patientService.validatePatientEmails(emails);

        return emails;
    }

    private String buildConfirmationUrl(Notification notification) {
        return String.format("%s/public/appointments/confirm/%d/%s", baseUrl, notification.getAppointment().getId_appointment(),
                             notification.getConfirmation_token());
    }

    private String buildDentistRegistrationUrl(String token) {
        return String.format("http://localhost:5173/registro/dentista?token=%s", token);
    }

    private String buildSecretaryRegistrationUrl(String token) {
        return String.format("http://localhost:5173/registro/secretario?token=%s", token);
    }

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
            case "CASH"         -> "Efectivo";
            case "CREDIT_CARD"  -> "Tarjeta de Crédito";
            case "DEBIT_CARD"   -> "Tarjeta de Débito";
            case "MERCADO_PAGO" -> "Mercado Pago";
            default             -> method;
        };
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}