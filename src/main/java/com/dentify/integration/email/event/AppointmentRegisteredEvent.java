package com.dentify.integration.email.event;

import com.dentify.domain.appointment.model.Appointment;

public record AppointmentRegisteredEvent(Appointment appointment) {
}
