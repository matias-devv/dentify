package com.floss.odontologia.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;
import java.util.List;

@Entity @Getter @Setter
public class Schedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_schedule;

    @Column( name = "duration_slot_minutes", nullable = false)
    private Integer duration_slot_minutes;
    private LocalTime start_time;
    private LocalTime end_time;

    @ManyToOne ( fetch = FetchType.LAZY)
    @JoinColumn( name = "id_agenda")
    private Agenda agenda;

    @OneToMany(mappedBy = "horario", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Day> days;
}
