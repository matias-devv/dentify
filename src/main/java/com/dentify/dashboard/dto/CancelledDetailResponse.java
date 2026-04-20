package com.dentify.dashboard.dto;

import com.dentify.domain.appointment.enums.AppointmentStatus;

import java.time.LocalDateTime;

public record CancelledDetailResponse(Long id_appointment,
                                      String time,
                                      String patient_name,
                                      String patient_surname,
                                      String cancelled_by,
                                      String reason_for_cancellation,
                                      String service_name,
                                      LocalDateTime appointmentStart) {
}
