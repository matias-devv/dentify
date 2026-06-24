package com.dentify.domain.appointment.event;

import com.dentify.domain.appointment.enums.AppointmentStatus;
import com.dentify.domain.payment.enums.PaymentMethod;
import com.dentify.domain.payment.enums.PaymentStatus;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 *  Evento de dominio: Turno creado con pago
 *
 * Se dispara después de que el appointment + payment + treatment se hayan persistido exitosamente.
 * Este evento dispara tareas asíncronas (PDF, emails, etc.)
 *
 * IMPORTANTE: Ser serializable para posibles distribuciones futuras (Kafka/RabbitMQ)
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class AppointmentCreatedWithPaymentEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    // ==================== APPOINTMENT DATA ====================
    private Long appointmentId;
    private LocalDate appointmentDate;
    private LocalTime appointmentStartTime;
    private LocalTime appointmentEndTime;
    private String appointmentNotes;
    private AppointmentStatus appointmentStatus;
    private Integer durationMinutes;


    // ==================== PAYMENT DATA ====================
    private Long paymentId;
    private PaymentMethod paymentMethod;
    private BigDecimal paymentAmount; // from the product (unit price)
    private Boolean isPaymentImmediate; // payNow = true

    // ==================== PATIENT DATA ====================
    private String patientEmail;
    private String patientName;
    private String patientSurname;
    private String patientDni;
    private String patientInstructions;

    // ==================== DENTIST DATA ====================
    private String dentistName;
    private String dentistSurname;
    private String dentistEmail;
    private String dentistPhone;
    private String clinicName;
    private String tenantId;

    // ==================== PRODUCT DATA ====================
    private String productName;
    private String productDescription;

    // ==================== TREATMENT DATA ====================
    private Long treatmentId;

    // ==================== FLAGS PARA ROUTING ====================

    /**
     * Si true: actualizar status a CONFIRMED
     * Caso: CASH + payNow = true (pago confirmado)
     */
    private Boolean shouldConfirmAppointment;

    // ==================== TIMESTAMPS ====================
    private Long createdAtMillis;

    @Override
    public String toString() {
        return "AppointmentCreatedWithPayEvent{" +
                "appointmentId=" + appointmentId +
                ", patientName='" + patientName + '\'' +
                ", paymentMethod=" + paymentMethod +
                ", shouldConfirmAppointment=" + shouldConfirmAppointment +
                '}';
    }
}
