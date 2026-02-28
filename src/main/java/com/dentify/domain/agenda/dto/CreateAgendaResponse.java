package com.dentify.domain.agenda.dto;

import com.dentify.domain.schedule.model.Schedule;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public record AgendaResponse(Long id_agenda,
                             String agenda_name,
                             Boolean active,
                             LocalDate start_date,
                             LocalDate final_date,
                             Long id_product,
                             String product_name,
                             Set<Schedule> schedules) {
}
