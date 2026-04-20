package com.dentify.domain.appointment.dto.request;

import com.dentify.domain.appointment.enums.CancellationActuator;

public record CancelAppointmentRequest(Long id_appointment,
                                       String reason_for_cancellation,
                                       CancellationActuator cancelledBy) {
}
