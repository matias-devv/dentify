package com.dentify.calendar.dto.response.day;

import com.dentify.domain.appointment.dto.response.DetailedAppointmentResponse;

import java.time.LocalTime;

public record DetailedSlotResponse (LocalTime startTime,
                                    LocalTime endTime,
                                    boolean availability,
                                    //if it is reserved
                                    DetailedAppointmentResponse appointment) {
}
