package com.floss.odontologia.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Entity @Getter @Setter @AllArgsConstructor
@NoArgsConstructor
@Table(name = "patients")
public class Patient {

    @Id
    @GeneratedValue(strategy= GenerationType.SEQUENCE)
    private Long id_patient;

    @Column(unique = true)
    private String dni;
    private String name;
    private String surname;
    private String age;
    private LocalDate date_of_birth;
    private String insurance;
    private String phone_number;

    @OneToMany (mappedBy = "patient")
    private List<Appointment> appointments;

    @OneToMany (mappedBy = "patient")
    private List<ResponsibleAdult> responsibleAdultList;

}
