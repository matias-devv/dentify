package com.floss.odontologia.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Entity @Getter @Setter @AllArgsConstructor @NoArgsConstructor
@Table ( name  = "agendas")
public class Agenda {

    @Id @GeneratedValue ( strategy = GenerationType.IDENTITY)
    private Long id_agenda;

    private String agenda_name;
    private Boolean active;

    @Column (nullable = false)
    private LocalDate start_date ;
    @Column (nullable = false)
    private LocalDate final_date;

    @ManyToOne( fetch = FetchType.LAZY,  cascade = CascadeType.ALL)
    @JoinColumn( name = "id_schedule")
    private List<Schedule> schedules;

    @ManyToOne ( fetch =  FetchType.LAZY)
    @JoinColumn ( name = "id_user")
    private AppUser app_user;

    @ManyToOne( fetch =  FetchType.LAZY)
    @JoinColumn ( name = "id_product", nullable = true)
    private Product product;
}
