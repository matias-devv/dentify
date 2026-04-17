package com.dentify.domain.agenda.service;

import com.dentify.domain.agenda.dto.request.CreateAgendaRequest;
import com.dentify.domain.agenda.dto.response.CreateAgendaResponse;
import com.dentify.domain.agenda.model.Agenda;
import com.dentify.domain.dentist.model.Dentist;
import com.dentify.domain.product.model.Product;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface IAgendaService {

    public CreateAgendaResponse save(CreateAgendaRequest request, String username);

    public Agenda findAgendaWithDentistById(Long idAgenda);

    void validateIfAgendaIsActive(Agenda agenda);

    void validateAgendaAvailability(Agenda agenda, @Future(message = "The date must be in the future.") LocalDate date, LocalTime start_time);

    void verifyIfThisAgendaBelongsToTheDentist(Agenda agenda, Dentist dentist);

    void validatAgendaToCreateAppointment(Agenda agenda, Dentist dentist,
                                   @NotBlank(message = "The date is mandatory")
                                   @Future(message = "The date must be in the future.") LocalDate date,
                                   @NotBlank(message = "The start time is mandatory") LocalTime starTime);

    void validateDateRangeInAgenda(Agenda agenda, LocalDate startDate, LocalDate endDate);

    void validateDateWithinAgendaRange(Agenda agenda, LocalDate requestedDate);

    Agenda findAgendaWithSchedules(@NotBlank Long idAgenda);

    List<CreateAgendaResponse> findAgendasByDentist(Long id, String username);

    List<CreateAgendaResponse> findAgendasByClinic(String username);

//
//    public String patchStatusAgenda(AgendaRequestDTO agendaRequestDTO);
//
//    public String editAgenda(AgendaRequestDTO agendaRequestDTO);
//
//    public @Nullable FullDailyResponseDTO getAllSlotsInDay(DayRequestDTO request);
//
//    public @Nullable WeekSummaryResponseDTO getAvailableSlotsInWeek(WeekDateRangeRequestDTO request);
//
//    public @Nullable MonthSummaryResponseDTO getSummaryOfTheMonth(MonthDateRangeRequestDTO request);

}
