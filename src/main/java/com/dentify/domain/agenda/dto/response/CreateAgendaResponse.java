package com.dentify.domain.agenda.dto.response;

import com.dentify.domain.schedule.dto.response.ScheduleResponse;

import java.time.LocalDate;
import java.util.List;

public record CreateAgendaResponse(Long id_agenda,
                                   String agenda_name,
                                   Boolean active,
                                   LocalDate start_date,
                                   LocalDate final_date,
                                   Long id_product,
                                   String product_name,
                                   List<ScheduleResponse> schedules) {
}
