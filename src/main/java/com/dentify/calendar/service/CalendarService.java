package com.dentify.calendar.service;

import com.dentify.calendar.dto.request.day.DetailedDayRequest;
import com.dentify.calendar.dto.request.month.MonthRequest;
import com.dentify.calendar.dto.request.week.WeekRequest;
import com.dentify.calendar.dto.response.day.DetailedDayResponse;
import com.dentify.calendar.dto.response.day.DetailedSlotResponse;
import com.dentify.calendar.dto.response.month.DailySummaryResponse;
import com.dentify.calendar.dto.response.month.MonthResponse;
import com.dentify.calendar.dto.response.week.DayResponse;
import com.dentify.calendar.dto.response.week.SlotResponse;
import com.dentify.calendar.dto.response.week.WeekResponse;
import com.dentify.domain.agenda.enums.AvailabilityState;
import com.dentify.domain.agenda.model.Agenda;
import com.dentify.domain.agenda.service.IAgendaService;
import com.dentify.domain.appointment.model.Appointment;
import com.dentify.domain.appointment.service.IAppointmentService;
import com.dentify.domain.schedule.model.Schedule;
import com.dentify.mapper.AppointmentMapper;
import com.dentify.mapper.CalendarMapper;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CalendarService implements ICalendarService{

    private final IAgendaService agendaService;
    private final IAppointmentService appointmentService;

    //mappers
    private final AppointmentMapper appointmentMapper;
    private final CalendarMapper calendarMapper;

    @Override
    public MonthResponse getMonthlySummary(MonthRequest request) {

        List<DailySummaryResponse> days = new ArrayList<>();

        Agenda agenda = agendaService.findAgendaWithSchedules( request.id_agenda() );

        agendaService.validateIfAgendaIsActive(agenda);

        YearMonth yearMonth = YearMonth.of( request.year(), Month.of( request.month_number() ) );

        LocalDateTime start = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime end = yearMonth.atEndOfMonth().atStartOfDay();

        agendaService.validateDateRangeInAgenda( agenda, start.toLocalDate(), end.toLocalDate() );

        List< Appointment> appointments = appointmentService.findAppointmentsByAgendaAndDateRange( agenda.getId_agenda(), start, end);

        Map< LocalDateTime, Appointment> mapAppointments = appointmentService.fillInAppointmentMap( appointments );

        Map< DayOfWeek, List<Schedule> > mapDays = agenda.fillMapDays();

        //Add 1 day so that it does not exclude the last day
        List<LocalDate> dates = start.toLocalDate().datesUntil( end.toLocalDate().plusDays(1) ).toList();

        for( LocalDate date : dates ) {

            DayOfWeek dayOfWeek = date.getDayOfWeek();

            List<Schedule> schedulesForDay = mapDays.get(dayOfWeek);

            DailySummaryResponse response = (schedulesForDay != null) ? buildDailySummaryForMultipleSchedules(date, schedulesForDay, mapAppointments)
                                                                      : buildNonWorkingDay(date);


            days.add(response);
        }

        return calendarMapper.buildMonthResponse( agenda, yearMonth, days);
    }

    private DailySummaryResponse buildDailySummaryForMultipleSchedules(LocalDate date, List<Schedule> schedulesForDay,
                                                                       Map<LocalDateTime, Appointment> mapAppointments) {

        int freeSlots = 0;
        int occupiedSlots = 0;

        for (Schedule schedule : schedulesForDay) {

            List<DetailedSlotResponse> slots = calculateDetailedSlotsListForThisSchedule(date, schedule, mapAppointments);

            freeSlots += this.countAvailableSlots(slots);
            occupiedSlots += this.countOcuppiedSlots(slots);
        }

        int totalSlots = freeSlots + occupiedSlots;

        AvailabilityState state = calculateAvailabilityState(freeSlots, totalSlots);

        return new DailySummaryResponse(date,
                                        date.getDayOfMonth(),
                                        date.getDayOfWeek(),
                                        freeSlots,
                                        occupiedSlots,
                                        totalSlots,
                                        state);
    }

    private DailySummaryResponse buildNonWorkingDay(LocalDate date) {
        return new DailySummaryResponse(date,
                                        date.getDayOfMonth(),
                                        date.getDayOfWeek(),
                                        0,
                                        0,
                                        0,
                                        AvailabilityState.NO_SCHEDULE);
    }

    private AvailabilityState calculateAvailabilityState(Integer freeSlots, Integer totalSlots) {

        if (totalSlots == 0) {
            return AvailabilityState.NO_SCHEDULE;
        }

        if (freeSlots == 0) {
            return AvailabilityState.FULL;
        }

        double percentage = (freeSlots * 100.0) / totalSlots;

        if (percentage < 30.0) {
            return AvailabilityState.LOW_AVAILABILITY;
        }

        return AvailabilityState.AVAILABLE;
    }

    @Override
    public @Nullable WeekResponse getWeeklySlots(WeekRequest request) {

        List<DayResponse> dayResponses = new ArrayList<>();

        Agenda agenda = agendaService.findAgendaWithSchedules( request.id_agenda() );

        agendaService.validateDateRangeInAgenda( agenda, request.startDate(), request.endDate());

        List<Appointment> appointments = appointmentService.findAppointmentsByAgendaAndDateRange( agenda.getId_agenda(),
                                                                                                  request.startDate().atStartOfDay(),
                                                                                                  request.endDate().atStartOfDay() );

        Map<LocalDateTime, Appointment> mapAppointments = appointmentService.fillInAppointmentMap( appointments );

        //Add 1 day so that it does not exclude the last day
        List<LocalDate> dates = request.startDate().datesUntil( request.endDate().plusDays(1) ).toList();

        Map< DayOfWeek, List<Schedule> > mapDays = agenda.fillMapDays();

        for( LocalDate date : dates ) {

            DayOfWeek dayOfWeek = date.getDayOfWeek();

            List<Schedule> schedulesForDay = mapDays.get(dayOfWeek);

            if(schedulesForDay != null) {

                    DayResponse response =  this.buildDayResponseListForMultipleSchedules( date, schedulesForDay, mapAppointments);

                    dayResponses.add(response);
            }else{
                    dayResponses.add( new DayResponse( date, date.getDayOfWeek(), false, null) );
            }
        }

        return calendarMapper.buildWeekResponse( agenda, dayResponses);
    }

    private DayResponse buildDayResponseListForMultipleSchedules( LocalDate date, List<Schedule> schedulesForDay,
                                                                  Map<LocalDateTime, Appointment> mapAppointments) {

        List<SlotResponse> slots = new ArrayList<>();

        List<Schedule> orderedList = this.orderSchedulesList(schedulesForDay);

        for ( Schedule schedule : orderedList) {

                List<SlotResponse> newSlots = calculateSlotsResponsesListForThisSchedule( date, schedule, mapAppointments);

                slots.addAll(newSlots);
        }
        return new DayResponse(date,
                               date.getDayOfWeek(),
                              true,
                               slots);
    }

    private List<Schedule>  orderSchedulesList(List<Schedule> schedulesForDay) {

        //Hour of reference 6:00 am
        LocalTime sixAm = LocalTime.of(6,0);

        //Sort: Nearest first
        schedulesForDay.sort( Comparator.comparingLong(s -> Math.abs( ChronoUnit.MINUTES.between( sixAm, s.getStart_time() ) ) ) );

        //Reference time example: 6:00 am, result: [09:30, 11:00, 14:00, 18:45]
       return schedulesForDay;
    }


    private List<SlotResponse> calculateSlotsResponsesListForThisSchedule( LocalDate date, Schedule schedule, Map<LocalDateTime, Appointment> mapAppointments) {

        List<SlotResponse> slots = new ArrayList<>();

        LocalTime currentTime = schedule.getStart_time();

        LocalTime endTime = schedule.getEnd_time();

        int duration = schedule.getAgenda().getDuration_minutes();

        // As long as the slot fits before the final hour
        while( !currentTime.plusMinutes(duration).isAfter(endTime) ) {

            LocalTime slotEnd = currentTime.plusMinutes(duration);

            LocalDateTime key = date.atTime(currentTime);

            Appointment appointment = mapAppointments.get(key);

            SlotResponse slot = (appointment != null)
                    ? new SlotResponse(currentTime, slotEnd, false, appointmentMapper.buildAppointmentResponse(appointment) )
                    : new SlotResponse(currentTime, slotEnd, true, null );

            currentTime = slotEnd; //I advance to the next slot

            slots.add( slot);
        }
        return slots;
    }

    @Override
    public DetailedDayResponse getDailySlots(DetailedDayRequest request) {

        List<DetailedSlotResponse> slots = new ArrayList<>();

        Agenda agenda = agendaService.findAgendaWithSchedules( request.id_agenda() );

        agendaService.validateDateWithinAgendaRange( agenda, request.startDate());

        List<Appointment> appointments = appointmentService.findAppointmentsByAgendaAndDate( agenda.getId_agenda(), request.startDate());

        Map<LocalDateTime, Appointment> mapAppointments = appointmentService.fillInAppointmentMap( appointments );

        DayOfWeek dayOfWeek =  request.startDate().getDayOfWeek();

        Map< DayOfWeek, List<Schedule> > mapDays = agenda.fillMapDays();

        List<Schedule> schedulesForDay = mapDays.get(dayOfWeek);

        if(schedulesForDay != null) {

            List<Schedule> orderedList = this.orderSchedulesList(schedulesForDay);

            for (Schedule schedule : orderedList) {

                List<DetailedSlotResponse> newSlots = this.calculateDetailedSlotsListForThisSchedule(request.startDate(), schedule, mapAppointments);

                slots.addAll(newSlots);
            }

            Integer freeSlots = this.countAvailableSlots(slots);
            Integer ocuppiedSlots = this.countOcuppiedSlots(slots);
            Integer totalSlots = freeSlots + ocuppiedSlots;

            return calendarMapper.buildDetailedDayResponse( agenda, slots, request.startDate(), totalSlots, freeSlots, ocuppiedSlots, null);
        }
        else{
            return calendarMapper.buildDetailedDayResponse( agenda, slots, request.startDate(), 0, 0, 0, "The day has no scheduled times");
        }
    }

    private List<DetailedSlotResponse> calculateDetailedSlotsListForThisSchedule(LocalDate date, Schedule schedule,
                                                                            Map<LocalDateTime, Appointment> mapAppointments) {

        List<DetailedSlotResponse> slots = new ArrayList<>();

        LocalTime currentTime = schedule.getStart_time();

        LocalTime endTime = schedule.getEnd_time();

        int duration = schedule.getAgenda().getDuration_minutes();

        // As long as the slot fits before the final hour
        while( !currentTime.plusMinutes(duration).isAfter(endTime) ) {

            LocalTime slotEnd = currentTime.plusMinutes(duration);

            LocalDateTime key = date.atTime(currentTime);

            Appointment appointment = mapAppointments.get(key);

            DetailedSlotResponse slot = (appointment != null)
                    ? new DetailedSlotResponse(currentTime, slotEnd, false, appointmentMapper.buildDetailedAppointmentResponse(appointment) )
                    : new DetailedSlotResponse(currentTime, slotEnd, true, null );

            currentTime = slotEnd; //I advance to the next slot

            slots.add( slot);
        }
        return slots;
    }

    private Integer countAvailableSlots(List<DetailedSlotResponse> slots) {
        int freeSlots = 0;

        for ( DetailedSlotResponse slot : slots) {
            if ( slot.availability() == true ){
                freeSlots++;
            }
        }
        return freeSlots;
    }

    private Integer countOcuppiedSlots(List<DetailedSlotResponse> slots) {
        int occupiedSlots = 0;

        for ( DetailedSlotResponse slot : slots) {
            if ( slot.availability() == false ){
                occupiedSlots++;
            }
        }
        return occupiedSlots;
    }

}
