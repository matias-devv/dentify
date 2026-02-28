package com.dentify.domain.dentist;


import com.dentify.domain.agenda.model.Agenda;
import com.dentify.domain.appointment.model.Appointment;
import com.dentify.domain.secretary.Secretary;
import com.dentify.domain.speciality.model.Speciality;
import com.dentify.domain.treatment.model.Treatment;
import com.dentify.domain.userProfile.model.UserProfile;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "dentists")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Dentist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String professional_license;

    // N:N with Speciality with intermediate table
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "dentist_specialities",
            joinColumns = @JoinColumn(name = "dentist_id"),
            inverseJoinColumns = @JoinColumn(name = "speciality_id")
    )
    private Set<Speciality> specialities = new HashSet<>();

    // 1:1 with UserProfile — Dentist is the owner
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_profile_id", nullable = false, unique = true)
    private UserProfile userProfile;

    // N:N with Secretary with intermediate table
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "dentist_secretaries",
                joinColumns = @JoinColumn(name = "dentist_id"),
                inverseJoinColumns = @JoinColumn(name = "secretary_id")
    )
    private Set<Secretary> secretaries = new HashSet<>();

    @Column(nullable = false)
    private boolean active = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    //1 dentist -> n agendas
    @OneToMany( mappedBy = "dentist" )
    private List<Agenda> agendas;

    //1 dentist -> n appointments
    @OneToMany( mappedBy = "dentist")
    private List<Appointment> appointments;

    //1 dentist -> n treatments
    @OneToMany( mappedBy = "dentist")
    private List<Treatment> treatments;
}
