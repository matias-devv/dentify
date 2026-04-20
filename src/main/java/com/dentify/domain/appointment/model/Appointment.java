package com.dentify.domain.appointment.model;

import com.dentify.domain.agenda.model.Agenda;
import com.dentify.domain.appointment.enums.AppointmentStatus;
import com.dentify.domain.clinic.model.Clinic;
import com.dentify.domain.dentist.model.Dentist;
import com.dentify.domain.notification.model.Notification;
import com.dentify.domain.payment.enums.PaymentMethod;
import com.dentify.domain.patient.model.Patient;
import com.dentify.domain.payment.enums.PaymentStatus;
import com.dentify.domain.payment.model.TreatmentPayment;
import com.dentify.domain.treatment.model.Treatment;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity @Getter @Setter @AllArgsConstructor @NoArgsConstructor @Table ( name = "appointments") @Builder
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_appointment;

    @Column(nullable = true)
    private String notes;

    @Column( nullable = true)
    private String patient_instructions;

    @Enumerated( EnumType.STRING)
    private AppointmentStatus appointmentStatus = AppointmentStatus.SCHEDULED;

    @Column( nullable = false)
    private LocalDateTime appointmentStart;

    @Column(nullable = false)
    private LocalDateTime appointmentEnd;

    @Column( nullable = false)
    private Integer duration_minutes;

    @Column(nullable = true)
    private String reason_for_cancellation;

    @Column(name = "attendance_confirmed")
    private Boolean attendanceConfirmed;

    private LocalDateTime confirmed_at;
    private LocalDateTime cancelled_at;

    Boolean lateArrival = false; // Marks that this appointment went through NO_SHOW → WALK_IN_PENDING → ADMITTED.

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "clinic_id", nullable = false)
    private Clinic clinic;

    // N:1 — The appointment belongs to a specific dentist
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "dentist_id", nullable = false)
    private Dentist dentist;

    // N:1 — siempre tiene un paciente
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    //one appointment -> n pays
    @OneToMany ( mappedBy = "appointment")
    private List<TreatmentPayment> payments;

    //one appointment -> n notifications
    @OneToMany ( mappedBy = "appointment")
    private List<Notification> notifications;

    // N:1 — el turno pertenece a una agenda
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "agenda_id", nullable = false)
    private Agenda agenda;

    // NULLABLE — un turno puede existir sin tratamiento (consulta suelta)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "treatment_id")
    private Treatment treatment;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public PaymentMethod getPrimaryPaymentMethod() {
        return this.payments.stream()
                            .findFirst()
                            .map(TreatmentPayment::getPayment_method)
                            .orElse(null);
    }

    public TreatmentPayment getPrimaryPayment() {

        // This is because 1 payment -> 1 appointment
        // There are N payments -> 1 appointment when the patient pays with MercadoPago and chooses to pay in installments.
        // But most of the time it's 1 payment -> 1 appointment
        return this.payments.stream()
                            .findFirst()
                            .orElseThrow(() -> new IllegalStateException("Appointment without payments. appointmentId=" + this.id_appointment));
    }

    public boolean isCancelled() {
        if ( this.getAppointmentStatus().equals( AppointmentStatus.CANCELLED_BY_SYSTEM)) return true;
        if ( this.getAppointmentStatus().equals( AppointmentStatus.CANCELLED_BY_DENTIST)) return true;
        if ( this.getAppointmentStatus().equals( AppointmentStatus.CANCELLED_BY_SECRETARY)) return true;
        if ( this.getAppointmentStatus().equals( AppointmentStatus.CANCELLED_BY_PATIENT)) return true;
        return false;
    }

    public boolean isInTerminalState() {

        AppointmentStatus status = this.getAppointmentStatus();

        // Can only cancel appointments that haven't been attended yet
        return status == AppointmentStatus.COMPLETED
                || status == AppointmentStatus.CANCELLED_BY_DENTIST
                || status == AppointmentStatus.CANCELLED_BY_SECRETARY
                || status == AppointmentStatus.CANCELLED_BY_SYSTEM
                || status == AppointmentStatus.CANCELLED_BY_PATIENT
                || status == AppointmentStatus.NO_SHOW;
    }

    public boolean isNotConfirmed() {
        return this.attendanceConfirmed.equals(false) && !this.appointmentStatus.equals(AppointmentStatus.CONFIRMED);
    }

    public boolean hasAConfirmedPay(){
        return this.getPayments().stream().anyMatch(p -> p.getPayment_status() == PaymentStatus.PAID);
    }

    public boolean isMarkWithNoShow() {
        return this.appointmentStatus == AppointmentStatus.NO_SHOW;
    }

    public boolean isAdmited(){
        return this.appointmentStatus == AppointmentStatus.ADMITTED;
    }

    public boolean isInAtention(){
        return this.appointmentStatus == AppointmentStatus.IN_ATTENTION;
    }

    public boolean isScheduled(){
        return this.appointmentStatus == AppointmentStatus.SCHEDULED;
    }

    public boolean isConfirmed(){
        return this.appointmentStatus == AppointmentStatus.CONFIRMED;
    }

    public boolean isWalkInPending() {
        return this.appointmentStatus == AppointmentStatus.WALK_IN_PENDING;
    }

    public boolean isCompleted() { return this.appointmentStatus == AppointmentStatus.COMPLETED; }
}
