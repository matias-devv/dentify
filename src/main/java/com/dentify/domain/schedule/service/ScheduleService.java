package com.dentify.domain.schedule.service;

import com.dentify.domain.agenda.dto.request.CreateAgendaRequest;
import com.dentify.domain.agenda.model.Agenda;
import com.dentify.domain.schedule.dto.request.ScheduleRequest;
import com.dentify.domain.schedule.model.Schedule;
import com.dentify.exception.schedule.InvalidScheduleException;
import com.dentify.exception.schedule.ScheduleOverlapException;
import com.dentify.mapper.ScheduleMapper;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduleService implements IScheduleService {

    private final ScheduleMapper scheduleMapper;

    @Override
    public void addSchedulesToAgenda( List<ScheduleRequest> schedules, Integer durationMinutes, Agenda agenda) {

         schedules.forEach(schedule -> {

             this.validateSchedules( schedule.startTime(), schedule.endTime(), schedule.days(), durationMinutes);

             this.validateOverlap( agenda, schedule.startTime(), schedule.endTime(), schedule.days() );

             Schedule newSchedule = scheduleMapper.setAttributesNewSchedule(schedule);

             agenda.addSchedule(newSchedule);
        });
    }

    private void validateOverlap( Agenda agenda, LocalTime startTime, LocalTime endTime, Set<DayOfWeek> days) {

         if ( agenda.getSchedules() != null ) {

             Map<DayOfWeek, List<Schedule> > daysMap = agenda.fillMapDays();

             for ( DayOfWeek day : days) {

                 if ( daysMap.get(day) != null ) {

                     List<Schedule> todaySchedules = daysMap.get(day);

                     for ( Schedule schedule : todaySchedules ) {

                         if( startTime.isBefore( schedule.getEnd_time() ) && endTime.isAfter( schedule.getStart_time() ) ) {
                             throw new ScheduleOverlapException("There is an overlap in schedules; " +
                                                                "please check if the times do not overlap with each other.");
                         }
                     }
                 }
             }
         }
    }

    private void validateSchedules(LocalTime startTime, LocalTime endTime, Set<DayOfWeek> days, Integer duration_minutes) {

        if ( startTime.isAfter(endTime) || endTime.isBefore(startTime) ) {
            throw new InvalidScheduleException("The final time needs to be after the start time");
        }
        if ( duration_minutes < 15) {
            throw new InvalidScheduleException("Minimum block duration is 15 minutes");
        }
        if ( duration_minutes > 480) {
            throw new InvalidScheduleException("Maximum block duration is 8 hours");
        }
        if ( days == null || days.isEmpty() ) {
            throw new InvalidScheduleException("Schedule must have at least one day selected");
        }

        long totalMinutes = Duration.between( startTime, endTime).toMinutes();

        if (totalMinutes < duration_minutes) {
            throw new InvalidScheduleException("Time range is shorter than block duration");
        }
    }

    @Override
    public void validateNullSchedules(CreateAgendaRequest request) {
        if ( request.schedules() == null || request.schedules().isEmpty() ){
            throw new InvalidScheduleException("The schedules are mandatory");
        }
    }
}
