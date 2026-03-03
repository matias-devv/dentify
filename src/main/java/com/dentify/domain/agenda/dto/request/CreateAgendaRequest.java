package com.dentify.domain.agenda.dto.request;

import com.dentify.domain.schedule.dto.request.ScheduleRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

public record CreateAgendaRequest(@NotBlank String agendaName,
                                  @NotNull LocalDate startDate,
                                  @NotNull LocalDate finalDate,
                                  @NotNull Integer duration_minutes,
                                  @NotNull Boolean active,

                                  Long idDentist, // nullable: null if the caller is DENTIST

                                  Long idProduct, // nullable: schedule with no assigned product

                                  @NotNull List<ScheduleRequest> schedules) {
}