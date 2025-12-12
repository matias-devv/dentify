package com.floss.odontologia.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity @Getter @Setter
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id_user;

    private String username;
    private String password;

    @OneToOne
    @JoinColumn(name="id_dentist")
    private Dentist dentist;

    @OneToOne
    @JoinColumn(name="id_secretary")
    private Secretary secretary;

    @ManyToOne
    @JoinColumn(name="id_role")
    private Role role;
}
