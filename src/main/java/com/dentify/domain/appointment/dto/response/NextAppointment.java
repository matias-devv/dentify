package com.dentify.domain.appointment.dto.response;

import java.time.LocalDateTime;

public record NextAppointment(LocalDateTime date,
                              String patient_name,
                              String patient_surname) {
}
