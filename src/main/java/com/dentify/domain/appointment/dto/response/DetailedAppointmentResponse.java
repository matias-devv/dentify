package com.dentify.domain.appointment.dto.response;

import com.dentify.domain.appointment.enums.AppointmentStatus;

public record DetailedAppointmentResponse(Long id,
                                          String patient_name,
                                          String patient_surname,
                                          String patient_phone_number,
                                          AppointmentStatus status,

                                          //optionals
                                          String product_name,
                                          String notes) {
}
