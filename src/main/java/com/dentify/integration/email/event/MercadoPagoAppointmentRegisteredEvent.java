package com.dentify.integration.email.event;

import com.dentify.domain.appointment.model.Appointment;

public record MercadoPagoAppointmentRegisteredEvent(Appointment appointment, String initPoint) {}