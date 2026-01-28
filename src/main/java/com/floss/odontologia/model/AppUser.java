package com.floss.odontologia.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity @Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id_app_user;

    private String name;
    private String surname;
    private String dni;

    @Column( unique = true, nullable = false)
    private String email;
    private String phone_number;

    @OneToOne
    @JoinColumn(name=" id_auth_user")
    private AuthUser auth_user;

    @ManyToMany ( fetch = FetchType.LAZY)
    @JoinTable ( name = "appUser_specialities", joinColumns = @JoinColumn ( name = "id_user_app"),
                 inverseJoinColumns = @JoinColumn ( name = "id_speciality"))
    private Set<Speciality> specialities = new HashSet<>();

    @OneToMany(mappedBy = "app_user")
    private List<Agenda> agendas;

    @OneToMany(mappedBy = "app_user")
    private List<Appointment> appointments;

    @OneToMany( mappedBy = "app_user")
    private List<Treatment> treatments;
}
