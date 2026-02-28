package com.dentify.calendar.dto.response;

import com.dentify.calendar.dto.PayResponse;
import com.dentify.domain.appointment.enums.AppointmentStatus;
import com.dentify.domain.dentist.dto.DentistResponse;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public record FullAppointmentResponse(Long id_appointment,
                                      AppointmentStatus status,
                                      LocalTime startTime,
                                      LocalTime endTime,
                                      Integer duration,
                                      Boolean attendanceConfirmed,
                                      LocalDateTime confirmed_at,
                                      PatientResponse patient,
                                      ProductResponse product,
                                      DentistResponse dentist,
                                      AgendaResponse agenda,
                                      TreatmentResponse treatment,
                                      List<PayResponse> pay,
                                      String notes,
                                      String patient_instructions,
                                      String reason_for_cancellation) {
}
