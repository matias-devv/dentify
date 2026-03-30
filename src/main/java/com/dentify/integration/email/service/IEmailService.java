package com.dentify.integration.email.service;

import com.dentify.domain.appointment.model.Appointment;
import com.dentify.domain.invitation.model.Invitation;
import com.dentify.domain.mercadopagopayment.model.MercadoPagoPayment;
import com.dentify.domain.notification.model.Notification;
import com.dentify.domain.pay.model.Pay;
import com.dentify.domain.receipt.model.Receipt;
import com.dentify.domain.userProfile.model.UserProfile;

public interface IEmailService {

    // =========================================================================
    // INVITATIONS
    // =========================================================================

    /**
     * Sends an onboarding invitation email to a new user (dentist or secretary).
     * Routes to the admin variant if {@code isAdmin} is true, otherwise to the dentist/clinic variant.
     *
     * @param invitation the invitation entity (token, role, target email)
     * @param inviter    UserProfile of whoever sent the invitation
     * @param isAdmin    true when the sender is a Dentify platform admin
     */
    void sendInvitation(Invitation invitation, UserProfile inviter, boolean isAdmin);

    // =========================================================================
    // CANCELLATIONS
    // =========================================================================

    /**
     * Notifies both patient and dentist of a manual appointment cancellation.
     * Called by {@code AppointmentService.cancelAppointment}.
     * Errors are logged silently — no exception is propagated.
     *
     * @param appointment the cancelled appointment (must contain reason and status)
     */
    void sendAppointmentManuallyCancelled(Appointment appointment);

    /**
     * Notifies the patient when the system auto-cancels an appointment due to
     * missing confirmation. Called by {@code AppointmentScheduler}.
     * Errors are logged silently — no exception is propagated.
     *
     * @param appointment the system-cancelled appointment
     */
    void sendAppointmentCancelledBySystem(Appointment appointment);

    // =========================================================================
    // CONFIRMATIONS AND REMINDERS
    // =========================================================================

    /**
     * Sends the 48-hour attendance confirmation request.
     * Routes to the MercadoPago or Cash variant based on the appointment's payment method.
     * Called by {@code AppointmentScheduler.sendConfirmationRemindersTwoDaysBefore}.
     * Errors are logged silently — no exception is propagated.
     *
     * @param notification the notification entity (holds appointment reference and confirmation token)
     */
    void sendConfirmationRequest(Notification notification);

    /**
     * Sends the urgent 24-hour attendance confirmation request.
     * Routes to the MercadoPago or Cash variant based on the appointment's payment method.
     * Called by {@code AppointmentScheduler.sendConfirmationRemindersOneDayBefore}.
     * Errors are logged silently — no exception is propagated.
     *
     * @param notification the notification entity (holds appointment reference and confirmation token)
     */
    void urgentConfirmationRequest(Notification notification);

    /**
     * Sends the final 5-hour reminder to an unconfirmed patient with a pending MercadoPago payment.
     * Includes a payment link so the patient can pay and confirm in one step.
     * Called by {@code AppointmentScheduler.processFinalAppointmentReminder}.
     * Errors are logged silently — no exception is propagated.
     *
     * @param appointment the upcoming appointment
     * @param pay         the associated payment (must have MercadoPago data populated)
     */
    void sendFinalConfirmationRemindingWithMercadoPago(Appointment appointment, Pay pay);

    /**
     * Sends the final 5-hour reminder to an unconfirmed patient with a pending cash payment.
     * No payment link is included. Called by {@code AppointmentScheduler.processFinalAppointmentReminder}.
     * Errors are logged silently — no exception is propagated.
     *
     * @param appointment the upcoming appointment
     */
    void sendFinalConfirmationReminder(Appointment appointment);

    /**
     * Sends a simple 5-hour reminder to a patient who has already confirmed attendance.
     * No confirmation button or payment link is included.
     * Called by {@code AppointmentScheduler.processFinalAppointmentReminder}.
     * Errors are logged silently — no exception is propagated.
     *
     * @param appointment the upcoming confirmed appointment
     */
    void sendSimpleFinalConfirmationReminder(Appointment appointment);

    // =========================================================================
    // RECEIPTS
    // =========================================================================

    /**
     * Full post-payment orchestration for cash payments made at booking time
     * ({@code payNow = true}). Creates notifications, sends the PDF receipt to the
     * patient, and sends appointment confirmation emails to both patient and dentist.
     * Called by {@code PayService.upgradeToPaidAppointment}.
     * This method is <strong>critical</strong>: exceptions propagate to the caller.
     *
     * @param appointment the appointment linked to the payment
     * @param pay         the confirmed cash payment
     * @param receipt     the generated PDF receipt
     */
    void sendCashPaymentReceipt(Appointment appointment, Pay pay, Receipt receipt);

    /**
     * Sends only the PDF receipt to the patient and dentist for a cash payment
     * confirmed later via the dashboard ({@code PATCH /api/payments/{id}/confirm-cash}).
     * No appointment confirmation emails or notifications are generated here,
     * since the appointment was already confirmed at booking.
     * Called by {@code PayService.confirmCashPayment}.
     * Errors are logged silently — no exception is propagated.
     *
     * @param appointment the appointment linked to the payment
     * @param pay         the confirmed cash payment
     * @param receipt     the generated PDF receipt
     */
    void sendCashReceiptToThePatient(Appointment appointment, Pay pay, Receipt receipt);

    /**
     * Full post-payment orchestration for MercadoPago payments approved via webhook.
     * Creates notifications, sends the PDF receipt to the patient, and sends appointment
     * confirmation emails to both patient and dentist.
     * Called by {@code PaymentProcessorService.handleApprovedPayment}.
     * This method is <strong>critical</strong>: exceptions propagate to the caller.
     *
     * @param appointment the appointment linked to the payment
     * @param pay         the approved payment
     * @param payMP       the MercadoPago payment details
     * @param receipt     the generated PDF receipt
     */
    void sendPaymentReceiptMercadoPago(Appointment appointment, Pay pay,
                                       MercadoPagoPayment payMP, Receipt receipt);

    // =========================================================================
    // NO-SHOW WARNINGS
    // =========================================================================

    /**
     * Sends a first-offense no-show warning to the patient.
     * Called by {@code AppointmentScheduler.categorizeWarningToSend}.
     * Errors are logged silently — no exception is propagated.
     *
     * @param appointment the appointment marked as NO_SHOW
     */
    void sendFirstNoShowWarning(Appointment appointment);

    /**
     * Sends a second-offense no-show warning to the patient.
     * Called by {@code AppointmentScheduler.categorizeWarningToSend}.
     * Errors are logged silently — no exception is propagated.
     *
     * @param appointment the appointment marked as NO_SHOW
     */
    void sendSecondNoShowWarning(Appointment appointment);

    /**
     * Sends a critical no-show warning (3+ offenses) to the patient.
     * Called by {@code AppointmentScheduler.categorizeWarningToSend}.
     * Errors are logged silently — no exception is propagated.
     *
     * @param appointment the appointment marked as NO_SHOW
     */
    void sendThirdNoShowWarning(Appointment appointment);
}