package com.dentify.scheduler;
import com.dentify.domain.appointment.enums.AppointmentStatus;
import com.dentify.domain.appointment.model.Appointment;
import com.dentify.domain.appointment.service.IAppointmentService;
import com.dentify.domain.notification.enums.ReminderWindow;
import com.dentify.domain.notification.service.INotificationService;
import com.dentify.domain.patientstat.model.PatientStat;
import com.dentify.domain.patientstat.service.IPatientStatService;
import com.dentify.domain.payment.enums.PaymentMethod;
import com.dentify.domain.payment.model.TreatmentPayment;
import com.dentify.integration.email.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class AppointmentScheduler {

    //services
    private final IAppointmentService appointmentService;
    private final INotificationService notificationService;
    private final IPatientStatService statService;
    private final EmailService emailService;


    /**
     * Enviar solicitud de confirmacion 48hs antes,
     * Ejecuta cada 3 horas
     */
    @Scheduled(cron = "0 0 */3 * * ?")
    public void sendConfirmationRemindersTwoDaysBefore() {

        log.info("🔔 Ejecutando solicitudes de confirmacion 48hs antes");

        LocalDateTime today = LocalDateTime.now();
        LocalDateTime twoDaysLater = today.plusDays(2);

        //Look for appointments that are scheduled, not confirmed, within today's date and the next two days.
        List<Appointment> appointments = appointmentService.findReservedAppointmentsNotConfirmedWithDetails(today, twoDaysLater);

        for (Appointment appointment : appointments) {

            notificationService.createReminderIfNotExists( appointment, ReminderWindow.TWO_DAYS )
                               .ifPresent(notification -> {

                                    try {
                                        emailService.sendConfirmationRequest(notification);

                                        notificationService.registerAttempt(notification, true, null);

                                    } catch (Exception e) {
                                        notificationService.registerAttempt(notification, false, e.getMessage());
                                    }
                                });
        }
    }

    /**
     * Enviar solicitud de confirmacion 24hs antes,
     * Ejecuta cada 3 horas
     */
    @Scheduled(cron = "0 0 */3 * * ?")
    public void sendConfirmationRemindersOneDayBefore() {

        log.info("🔔 Ejecutando solicitudes de confirmacion 24hs antes");

        LocalDateTime today = LocalDateTime.now();
        LocalDateTime oneDayLater = today.plusDays(1);

        //Look for appointments that are scheduled, not confirmed, within today's date and the next two days.
        List<Appointment> appointments = appointmentService.findReservedAppointmentsNotConfirmedWithDetails(today, oneDayLater);

        for (Appointment appointment : appointments) {

            notificationService.createReminderIfNotExists( appointment, ReminderWindow.ONE_DAY )
                               .ifPresent(notification -> {

                                    try {
                                        emailService.urgentConfirmationRequest(notification);

                                        notificationService.registerAttempt(notification, true, null);

                                    } catch (Exception e) {
                                        notificationService.registerAttempt(notification, false, e.getMessage());
                                    }
                                });
        }
    }

    /**
     * Verifica turnos no confirmados y cancela turnos si es necesario,
     * Ejecuta desde 7am hasta 8pm
     */
    @Scheduled(cron = "0 0 7-20 * * *")
    @Transactional
    public void cancelUnconfirmedAppointments() {
        log.info("Verificando turnos no confirmados");

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime next24Hours = now.plusHours(24);

        //Search for scheduled appointments within the next 24 hours
        List<Appointment> upcomingAppointments = appointmentService.findReservedAppointmentsNotConfirmedWithDetails(now, next24Hours);

        for (Appointment appointment : upcomingAppointments) {

                this.processAppointmentsWithLackOfConfirmation(appointment);
        }
    }

    private void processAppointmentsWithLackOfConfirmation(Appointment appointment) {

        long remainingHours = calculateRemainingHours( appointment );

        log.warn(" Appointment {} without confirmation - {} hours remaining", appointment.getId_appointment(), remainingHours);

        if ( remainingHours <= 3 && !appointment.isInTerminalState() ) {

            log.error("Appointment {} cancellation due to non-confirmation", appointment.getId_appointment());

            appointmentService.cancelAppointment( AppointmentStatus.CANCELLED_BY_SYSTEM, appointment, "Appointment not confirmed on time" );

            try {
                emailService.sendAppointmentCancelledBySystem(appointment);
                // whatsAppService.sendAppointmentCancelledBySystem(appointment);
            } catch (Exception e) {
                log.error("Error sending cancellation notification: {}", e.getMessage());
            }
        }
    }

    /**
     * Enviar recordatorio final si faltan menos de 5 horas para la cita,
     * Ejecuta cada 3 horas
     */
    @Scheduled(cron = "0 0 */3 * * ?")
    @Transactional
    public void sendFinalReminders() {

        log.info("Running final 5-hour reminder");

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime fourHoursLater = now.plusHours(5);

        //Look for unconfirmed appointments within 3 to 4 hours from now
        List<Appointment> appointments = appointmentService.findScheduledAppointmentsWithDetails( now, fourHoursLater);

            for (Appointment appointment : appointments) {

                processFinalAppointmentReminder(appointment);
            }
    }

    private void processFinalAppointmentReminder(Appointment appointment) {

        TreatmentPayment payment = appointment.getPrimaryPayment();

        if (payment == null) {
            log.warn("Appointment {} without associated payment", appointment.getId_appointment());
            return;
        }
        if ( appointment.isCancelled() ) {
            log.info("Appointment {} already cancelled, skipping reminder", appointment.getId_appointment());
            return;
        }
        try {
            if ( appointment.isNotConfirmed() ) {

                if (payment.getPayment_method() == PaymentMethod.MERCADO_PAGO) {

                    //Appointment with pending MP -> Email with confirmation button + payment link
                    emailService.sendFinalConfirmationRemindingWithMercadoPago(appointment, payment);

                } else if (payment.getPayment_method() == PaymentMethod.CASH) {

                    //Appointment with pending CASH -> Email with confirmation button only
                    emailService.sendFinalConfirmationReminder(appointment);
                }

                log.info("Reminder sent for appointment {} ", appointment.getId_appointment());
            }
            else{
                //Simple reminder, without a confirmation button or payment link
                emailService.sendSimpleFinalConfirmationReminder(appointment);
            }

        } catch (Exception e) {
            log.error("Error sending appointment reminder {}: {}",
                    appointment.getId_appointment(), e.getMessage());
        }
    }

    private long calculateRemainingHours(Appointment appointment) {

        LocalDateTime appointmentTime = appointment.getAppointmentStart();

        LocalDateTime now = LocalDateTime.now();

        return Duration.between( now, appointmentTime ).toHours();
    }

    /**
     * Marca como NO_SHOW los turnos que ya pasaron
     * Ejecuta cada 30 minutos
     */
    @Scheduled(cron = "0 */30 * * * ?")
    @Transactional
    public void markNoShowsAndUpdatePatientStats() {

        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        LocalDateTime today =  LocalDate.now().atStartOfDay();

        List<Appointment> expiredAppointments = appointmentService.findByDateLessThanEqualAndAppointmentStatusInWithDetails(
                                                                     today,
                                                                     List.of(AppointmentStatus.SCHEDULED, AppointmentStatus.CONFIRMED) );

        for (Appointment appointment : expiredAppointments) {

            LocalDateTime appointmentDateTime = appointment.getAppointmentStart();

            //If an hour has already passed and the appointment start time is before that time -> markNoShow
            if ( appointmentDateTime.isBefore( oneHourAgo )  ) {

                if ( appointment.getAppointmentStatus() != AppointmentStatus.NO_SHOW){

                        appointmentService.markNoShow(appointment);

                        PatientStat patientStat = statService.actualizeStatsForNoShow( appointment, appointment.getPatient());

                        log.info("Paciente marcado como NO_SHOW | AppointmentID: {} | PatientID: {} | RiskLevel: {}", appointment.getId_appointment(),
                                                                                                                      appointment.getPatient().getId_patient(),
                                                                                                                      patientStat.getRiskLevel());

                        this.categorizeWarningToSend(patientStat, appointment);
                    }
                }
            }
        }

    private void categorizeWarningToSend(PatientStat patientStat, Appointment appointment) {

        if ( patientStat.getTotalNoShows() == 1) {
            emailService.sendFirstNoShowWarning(appointment);

        } else if ( patientStat.getNoShowsLast30Days() == 2) {
            emailService.sendSecondNoShowWarning(appointment);

        } else if ( patientStat.getNoShowsLast90Days() >= 3) {
            emailService.sendThirdNoShowWarning(appointment);
        }
    }

}