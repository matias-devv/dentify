package com.dentify.domain.schedule.dto;

import jakarta.validation.constraints.NotNull;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Set;

public record ScheduleRequest(@NotNull LocalTime startTime,
                              @NotNull LocalTime endTime,
                              @NotNull Set<DayOfWeek> days) {

}