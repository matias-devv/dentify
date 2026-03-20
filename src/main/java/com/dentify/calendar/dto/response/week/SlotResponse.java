package com.dentify.calendar.dto.response.week;


import com.dentify.domain.appointment.dto.response.AppointmentResponse;

import java.time.LocalTime;

public record SlotResponse(LocalTime startTime,
                           LocalTime endTime,
                           boolean availability,
                           //if it is reserved
                           AppointmentResponse appointment) {
}
