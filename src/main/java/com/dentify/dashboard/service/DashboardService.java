package com.dentify.dashboard.service;

import com.dentify.dashboard.dto.DashboardAlert;
import com.dentify.dashboard.dto.DashboardSummary;
import com.dentify.dashboard.enums.DashboardAlertType;
import com.dentify.domain.appointment.enums.AppointmentStatus;
import com.dentify.domain.appointment.model.Appointment;
import com.dentify.domain.appointment.service.IAppointmentService;
import com.dentify.domain.dentist.model.Dentist;
import com.dentify.domain.dentist.service.IDentistService;
import com.dentify.domain.pay.enums.PaymentStatus;
import com.dentify.domain.pay.model.Pay;
import com.dentify.domain.pay.service.IPayService;
import com.dentify.domain.treatment.enums.TreatmentStatus;
import com.dentify.mapper.AppointmentMapper;
import com.dentify.mapper.DashboardMapper;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class DashboardService implements IDashboardService {

    private final IAppointmentService appointmentService;
    private final AppointmentMapper appointmentMapper;
    private final IPayService payService;
    private final IDentistService dentistService;

    //mappers
    private final DashboardMapper dashboardMapper;

    public DashboardSummary getDashboardSummary() {

        List<AppointmentStatus> statuses = List.of( AppointmentStatus.CANCELLED_BY_SYSTEM, AppointmentStatus.CANCELLED_BY_DENTIST,
                                                    AppointmentStatus.CANCELLED_BY_SECRETARY , AppointmentStatus.CANCELLED_BY_PATIENT);

        BigDecimal dailyIncome = payService.getDailyIncome();
        BigDecimal monthlyIncome = payService.getMonthlyIncome();
        Long appointmentsToday = appointmentService.countAppointmentsTodayExcludingStatuses(statuses);
        Optional<Appointment> appointment = appointmentService.findNextAppointment();

        return appointment.map(appo -> new DashboardSummary(dailyIncome,
                                                                monthlyIncome,
                                                                appointmentsToday,
                                                                appointmentMapper.buildNextAppointment(appo))).orElseGet(() -> new DashboardSummary(dailyIncome,
                                                                                                                                                    monthlyIncome,
                                                                                                                                                    appointmentsToday,
                                                                                                                                                   null));

    }

    @Override
    public List<DashboardAlert> getAlertsToday(String username) {

        Dentist dentist = dentistService.findDentistByAuthUserUsername(username);

        List<DashboardAlert> alerts = new ArrayList<>();

        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);

        this.fillPaymentAlerts( alerts, dentist.getId(),  startOfDay, endOfDay);

        this.fillAppointmentAlerts( alerts, dentist.getId(),  startOfDay, endOfDay);

        if ( !alerts.isEmpty() ){

            alerts.sort(Comparator.comparing(DashboardAlert::type));

            return alerts;
        }
        else return List.of();
    }

    private void fillPaymentAlerts(List<DashboardAlert> alerts, Long dentistId, LocalDateTime startOfDay, LocalDateTime endOfDay) {

        List<Pay> payments = payService.findPaymentsToday( dentistId, startOfDay, endOfDay);

        if ( !payments.isEmpty() ) {
            payments.forEach(pay -> {

                this.resolvePaymentTypeAlert(pay, alerts);
                }
            );
        }
    }

    private void resolvePaymentTypeAlert(Pay pay, List<DashboardAlert> alerts) {

        if ( pay.isPending() ) alerts.add( dashboardMapper.buildPendingPayAlert(pay) );

        else if ( pay.isPartial() ) alerts.add( dashboardMapper.buildPartialPayAlert(pay) );
    }

    private void fillAppointmentAlerts(List<DashboardAlert> alerts, Long dentistId, LocalDateTime startOfDay, LocalDateTime endOfDay) {

        List<Appointment> appointments = appointmentService.findAppointmentAlertsToday( dentistId, startOfDay, endOfDay);

        if ( !appointments.isEmpty() ) {

            appointments.forEach( a -> {

                this.resolveAppointmentTypeAlert(a, alerts);
            });
        }
    }

    private void resolveAppointmentTypeAlert(Appointment appointment, List<DashboardAlert> alerts) {

        if ( appointment.getTreatment().isAbandoned() ) alerts.add(dashboardMapper.buildAbandonedTreatment(appointment) );

        else if ( appointment.isMarkWithNoShow() ) alerts.add(dashboardMapper.buildNoShowAppointment(appointment) );
    }


}
