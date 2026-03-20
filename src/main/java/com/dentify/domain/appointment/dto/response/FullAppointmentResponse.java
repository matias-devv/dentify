package com.dentify.domain.appointment.dto.response;

import com.dentify.domain.pay.dto.response.PayResponse;
import com.dentify.domain.product.dto.response.ProductResponse;
import com.dentify.domain.treatment.dto.TreatmentResponse;
import com.dentify.domain.agenda.dto.response.AgendaResponse;
import com.dentify.domain.appointment.enums.AppointmentStatus;
import com.dentify.domain.dentist.dto.DentistResponse;
import com.dentify.domain.patient.dto.response.PatientResponse;

import java.time.LocalDateTime;
import java.util.List;

public record FullAppointmentResponse(Long id_appointment,
                                      AppointmentStatus status,
                                      LocalDateTime startTime,
                                      LocalDateTime endTime,
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
