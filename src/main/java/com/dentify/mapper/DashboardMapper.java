package com.dentify.mapper;

import com.dentify.dashboard.dto.DashboardAlert;
import com.dentify.dashboard.enums.DashboardAlertType;
import com.dentify.domain.appointment.model.Appointment;
import com.dentify.domain.pay.model.Pay;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

@Component
public class DashboardMapper {

    public DashboardAlert buildPendingPayAlert(Pay pay) {

        return new DashboardAlert(DashboardAlertType.PAYMENT_PENDING,
                pay.getId_pay(),
                pay.getAppointment().getPatient().getName(),
                pay.getAppointment().getPatient().getSurname(),
                pay.getAppointment().getAppointmentDate().toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")),
                "Pago pendiente de " + pay.getAmount() + " — sin confirmar");
    }

    public DashboardAlert buildPartialPayAlert(Pay pay) {
        return new DashboardAlert(DashboardAlertType.PARTIAL_PAYMENT,
                pay.getId_pay(),
                pay.getAppointment().getPatient().getName(),
                pay.getAppointment().getPatient().getSurname(),
                pay.getAppointment().getAppointmentDate().toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")),
                "Pago parcial — saldo restante " + pay.getTreatment().getOutstanding_balance());
    }

    public DashboardAlert buildAbandonedTreatment(Appointment a) {

        //one of the two must be greater than two
        Integer absences = ( a.getPatient().getPatient_stat().getNo_shows_last_30_days() != null ) ? a.getPatient().getPatient_stat().getNo_shows_last_30_days() :
                                                                                                     a.getPatient().getPatient_stat().getNo_shows_last_90_days();

        return new DashboardAlert(DashboardAlertType.TREATMENT_ABANDONED,
                a.getId_appointment(),
                a.getPatient().getName(),
                a.getPatient().getSurname(),
                a.getAppointmentDate().toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")),
                "Tratamiento con " + absences + " ausencias previas — en estado Abandonado");
    }

    public DashboardAlert buildNoShowAppointment(Appointment a) {
        return new DashboardAlert(DashboardAlertType.NO_SHOW_REGISTERED,
                a.getId_appointment(),
                a.getPatient().getName(),
                a.getPatient().getSurname(),
                a.getAppointmentDate().toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")),
                "No confirmo el turno");
    }

}
