package com.dentify.domain.agenda.dto.request;

import com.dentify.domain.schedule.dto.request.ScheduleRequest;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

public record CreateAgendaRequest(@NotBlank(message = "the agenda name is required")
                                  String agendaName,

                                  @NotNull(message = "startDate is required")
                                  LocalDate startDate,

                                  @NotNull(message = "finalDate is required")
                                  @Future(message = "finalDate must be in the future")
                                  LocalDate finalDate,

                                  @NotNull(message = "duration_minutes is required")
                                  Integer duration_minutes,

                                  @NotNull(message = "active flag is required")
                                  Boolean active,

                                  Long idDentist,

                                  Long idProduct, // nullable: schedule with no assigned product

                                  @NotNull(message = "schedules list is required")
                                  List<ScheduleRequest> schedules) {
}