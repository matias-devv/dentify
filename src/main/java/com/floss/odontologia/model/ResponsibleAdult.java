package com.floss.odontologia.model;

import com.floss.odontologia.enums.Relation;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity @Getter @Setter @AllArgsConstructor
@NoArgsConstructor
@Table(name = "responsible_adults")
public class ResponsibleAdult {

    @Id
    @GeneratedValue (strategy = GenerationType.SEQUENCE)
    private Long id_responsible;

    @Column(unique = true)
    private String dni;
    private String name;
    private String surname;
    private String phone_number;
    private String email;
    @Enumerated (EnumType.STRING)
    private Relation relacionEnum;

    @ManyToOne
    @JoinColumn(name="id_patient")
    private Patient patient;
}
