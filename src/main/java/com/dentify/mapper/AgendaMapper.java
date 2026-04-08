package com.dentify.mapper;

import com.dentify.domain.agenda.dto.response.AgendaResponse;
import com.dentify.domain.agenda.dto.request.CreateAgendaRequest;
import com.dentify.domain.agenda.dto.response.CreateAgendaResponse;
import com.dentify.domain.agenda.model.Agenda;
import com.dentify.domain.appointment.model.Appointment;
import com.dentify.domain.schedule.dto.response.ScheduleResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AgendaMapper {

    private final ScheduleMapper scheduleMapper;

    public Agenda setAttributesNewAgenda(CreateAgendaRequest createAgendaRequest) {
        return Agenda.builder()
                .agenda_name(createAgendaRequest.agendaName())
                .active(createAgendaRequest.active())
                .start_date(createAgendaRequest.startDate())
                .final_date(createAgendaRequest.finalDate())
                .duration_minutes(createAgendaRequest.duration_minutes())
                .build();

    }

    public CreateAgendaResponse createAgendaResponse(Agenda agenda) {

        List<ScheduleResponse> schedules = scheduleMapper.getListOfScheduleResponse(agenda);

        return new CreateAgendaResponse(
                agenda.getId_agenda(),
                agenda.getAgenda_name(),
                agenda.getActive(),
                agenda.getStart_date(),
                agenda.getFinal_date(),
                agenda.getProduct() != null ? agenda.getProduct().getId_product() : null,
                agenda.getProduct() != null ? agenda.getProduct().getNameProduct() : null,
                schedules);
    }

    public AgendaResponse buildSimpleAgendaResponse(Appointment appointment) {
        var agenda = appointment.getAgenda();

        if (agenda == null) return null;

        return new AgendaResponse(
                agenda.getId_agenda(),
                agenda.getAgenda_name()
        );
    }
}
