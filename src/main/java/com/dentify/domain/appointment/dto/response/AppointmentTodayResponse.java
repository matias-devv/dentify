package com.dentify.domain.appointment.dto.response;

import com.dentify.domain.appointment.enums.AppointmentStatus;

public record AppointmentTodayResponse(Long   id,
                                        String time,                  // format HH:mm
                                        String patient_name,
                                        String patient_surname,
                                        Long   patient_id,            // to navigate to patient details
                                        String coverage,
                                        AppointmentStatus status,
                                        boolean attendanceConfirmed,  // True, if already went through admissions
                                        String  serviceName           // nullable, product name/agenda
) {}
