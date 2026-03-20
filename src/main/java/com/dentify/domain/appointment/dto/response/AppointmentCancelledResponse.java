package com.dentify.domain.appointment.dto.response;

import com.dentify.domain.appointment.enums.AppointmentStatus;
import com.dentify.domain.patient.dto.response.CancelledPatientResponse;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record AppointmentCancelledResponse(Long id_appointment,
                                           AppointmentStatus status,
                                           LocalDate date,
                                           LocalTime startTime,
                                           LocalTime endTime,
                                           String reason_for_cancellation,
                                           LocalDateTime cancelledAt,
                                           CancelledPatientResponse patient) {
}
