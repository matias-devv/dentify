package com.dentify.domain.appointment.dto.request;

import com.dentify.domain.payment.enums.PaymentMethod;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

public record CreateAppointmentRequestDTO(@NotNull(message = "The patient is mandatory")
                                          Long id_patient,

                                          @NotNull(message = "The dentist is mandatory")
                                          Long id_dentist,

                                          @NotNull(message = "The agenda is mandatory")
                                          Long id_agenda,

                                          @NotNull(message = "The product is mandatory")
                                          Long id_product,

                                          @NotNull(message = "The date is mandatory")
                                       //   @Future(message = "The date must be in the future.")
                                          LocalDate date,

                                          @NotNull(message = "The start time is mandatory")
                                          LocalTime start_time,

                                          @NotNull(message = "The duration in minutes is mandatory")
                                          Integer duration_minutes,

                                          @NotNull(message = "The payment method is required")
                                          PaymentMethod paymentMethod,

                                          Boolean payNow,

                                          //optional fields
                                          String notes,
                                          String patient_instructions){
}
