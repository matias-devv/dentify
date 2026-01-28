package com.floss.odontologia.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Entity @Getter @Setter @AllArgsConstructor @NoArgsConstructor @Table ( name = "appointments")
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id_appointment;

    @Column(nullable = true)
    private String notes;

    @Column( nullable = true)
    private String patient_instructions;

    @Enumerated( EnumType.STRING)
    private AppointmentStatus appointmentStatus;

    @Column( nullable = false)
    private LocalDate date;

    @Column( nullable = false)
    private LocalTime startTime;

    @Column( nullable = false)
    private Integer duration_minutes;

    @Column(nullable = true)
    private String reason_for_cancellation;

    @ManyToOne
    @JoinColumn( name = "id_app_user", nullable = false)
    private AppUser app_user;

    @ManyToOne
    @JoinColumn( name = "id_patient", nullable = false)
    private Patient patient;

    //one appointment -> n pays
    @OneToMany ( mappedBy = "appointment")
    private List<Pay> pays;

    //one appointment -> n notifications
    @OneToMany ( mappedBy = "appointment")
    private List<Notification> notifications;

    //n appointments -> one treatment
    @ManyToOne
    @JoinColumn( name = "id_treatment", nullable = true)
    private Treatment treatment;

    //many appointments -> one agenda
    @ManyToOne
    @JoinColumn( name = "id_patient", nullable = false)
    private Agenda agenda;

}
