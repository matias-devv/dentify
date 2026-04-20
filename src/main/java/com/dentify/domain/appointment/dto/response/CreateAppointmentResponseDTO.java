package com.dentify.domain.appointment.dto.response;

import com.dentify.domain.appointment.enums.AppointmentStatus;
import com.dentify.domain.payment.enums.PaymentMethod;
import com.dentify.domain.payment.enums.PaymentStatus;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Builder
public record CreateAppointmentResponseDTO(Long id_appointment,
                                           Long id_pay,

                                           // Appointment info
                                           LocalDate date,
                                           LocalTime start_time,
                                           Integer duration_minutes,
                                           LocalTime end_time,

                                           // Payment info
                                           BigDecimal amount_to_pay,
                                           PaymentMethod payment_method,
                                           String payment_link,

                                           // Status
                                           AppointmentStatus appointment_status,
                                           PaymentStatus payment_status,

                                           // Product info
                                           String product_name) {
}
