package com.dentify.mapper;

import com.dentify.dashboard.dto.CancelledDetailResponse;
import com.dentify.dashboard.dto.CancelledTodayResponse;
import com.dentify.dashboard.dto.DashboardAlert;
import com.dentify.dashboard.enums.DashboardAlertType;
import com.dentify.domain.appointment.model.Appointment;
import com.dentify.domain.payment.model.TreatmentPayment;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class DashboardMapper {

    public DashboardAlert buildPendingPayAlert(TreatmentPayment payment) {

        return new DashboardAlert(DashboardAlertType.PAYMENT_PENDING,
                payment.getId_pay(),
                payment.getAppointment().getPatient().getName(),
                payment.getAppointment().getPatient().getSurname(),
                payment.getAppointment().getAppointmentStart().toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")),
                "Pago pendiente de " + payment.getAmount() + " — sin confirmar");
    }

    public DashboardAlert buildPartialPayAlert(TreatmentPayment payment) {
        return new DashboardAlert(DashboardAlertType.PARTIAL_PAYMENT,
                payment.getId_pay(),
                payment.getAppointment().getPatient().getName(),
                payment.getAppointment().getPatient().getSurname(),
                payment.getAppointment().getAppointmentStart().toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")),
                "Pago parcial — saldo restante " + payment.getTreatment().getOutstanding_balance());
    }

    public DashboardAlert buildAbandonedTreatment(Appointment a) {

        //one of the two must be greater than two
        Integer absences = ( a.getPatient().getPatient_stat().getNo_shows_last_30_days() != null ) ? a.getPatient().getPatient_stat().getNo_shows_last_30_days() :
                                                                                                     a.getPatient().getPatient_stat().getNo_shows_last_90_days();

        return new DashboardAlert(DashboardAlertType.TREATMENT_ABANDONED,
                a.getId_appointment(),
                a.getPatient().getName(),
                a.getPatient().getSurname(),
                a.getAppointmentStart().toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")),
                "Tratamiento con " + absences + " ausencias previas — en estado Abandonado");
    }

    public DashboardAlert buildNoShowAppointment(Appointment a) {
        return new DashboardAlert(DashboardAlertType.NO_SHOW_REGISTERED,
                a.getId_appointment(),
                a.getPatient().getName(),
                a.getPatient().getSurname(),
                a.getAppointmentStart().toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")),
                "No confirmo el turno");
    }

    public CancelledTodayResponse buildCancelledTodayResponse(List<CancelledDetailResponse> alerts) {
        return new CancelledTodayResponse(alerts,
                                          alerts.size());
    }

    public CancelledDetailResponse buildCancelledDetailResponse(Appointment a) {

        return new CancelledDetailResponse(a.getId_appointment(),
                                           a.getAppointmentStart().toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")),
                                           a.getPatient().getName(),
                                           a.getPatient().getSurname(),
                                           a.getAppointmentStatus().name(),
                                           a.getReason_for_cancellation(),
                                           a.getTreatment().getProduct().getNameProduct(),
                                           a.getAppointmentStart() );
    }
}
