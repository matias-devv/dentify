package com.floss.odontologia.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.DayOfWeek;

@Entity @AllArgsConstructor @NoArgsConstructor @Getter @Setter @Table ( name = "days")
public class Day {

    @Id @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long id_day;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_schedule", nullable = false)
    private Schedule schedule;

    @Enumerated(EnumType.STRING)
    @Column ( nullable =  false )
    private DayOfWeek dayOfWeek;
}
