package com.floss.odontologia.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Entity @Getter @Setter
public class Patient {

    @Id
    @GeneratedValue(strategy= GenerationType.SEQUENCE)
    private Long id_patient;

    @Basic
    private String name;
    private String surname;
    private String dni;
    private String age;
    private LocalDate date_of_birth;
    private Boolean insurance;
    private String patient_condition; // if I use only the word "condition" -> error because is a reserved word for SQL
    private Boolean routine;
    private String treatment_type;

    @OneToMany (mappedBy = "patient")
    private List<Appointment> appointments;

    @OneToMany (mappedBy = "patient")
    private List<ResponsibleAdult> responsibleAdultList;

}
