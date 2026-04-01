package com.dentify.domain.agenda.model;

import com.dentify.domain.appointment.model.Appointment;
import com.dentify.domain.clinic.model.Clinic;
import com.dentify.domain.dentist.model.Dentist;
import com.dentify.domain.schedule.model.Schedule;
import com.dentify.domain.product.model.Product;
import com.dentify.security.multitenancy.TenantEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;

@Entity @Getter @Setter @AllArgsConstructor @NoArgsConstructor
@Table ( name  = "agendas") @Builder
public class Agenda extends TenantEntity {

    @Id @GeneratedValue ( strategy = GenerationType.IDENTITY)
    private Long id_agenda;

    private String agenda_name;
    private Boolean active;

    @Column (nullable = false)
    private LocalDate start_date;

    @Column (nullable = false)
    private LocalDate final_date;

    @Column( name = "duration_minutes", nullable = false)
    private Integer duration_minutes;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "clinic_id", nullable = false)
    private Clinic clinic;

    // N:1 — la agenda siempre pertenece a un dentista
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "dentist_id", nullable = false)
    private Dentist dentist;

    // n agendas -> 1 product
    @ManyToOne( fetch =  FetchType.LAZY)
    @JoinColumn ( name = "id_product", nullable = true)
    private Product product;

    // 1 agenda -> n schedules
    @OneToMany ( mappedBy = "agenda", cascade = CascadeType.ALL)
    private Set<Schedule> schedules = new HashSet<>();

    // 1 agenda -> n appointments
    @OneToMany ( mappedBy = "agenda")
    private List<Appointment> appointments;

    //helper method para bidirectional sync
    public void addSchedule(Schedule schedule) {

        if ( this.schedules != null) this.schedules.add(schedule);
        else{
            Set<Schedule> schedules = new HashSet<>();

            schedules.add(schedule);

            this.schedules = schedules;
        }
        schedule.setAgenda(this);
    }

    public void removeSchedule(Schedule schedule) {
        schedules.remove(schedule);
        schedule.setAgenda(null);
    }

    public Map<DayOfWeek, List<Schedule>> fillMapDays() {

        Map< DayOfWeek, List<Schedule> > mapDays = new HashMap<>();

        this.schedules.forEach(schedule -> {
                                                        schedule.getDays().forEach(day -> {
                                                            mapDays
                                                                    .computeIfAbsent(day, d -> new ArrayList<>())
                                                                    .add(schedule);
                                                        });
        });

        return mapDays;
    }
}
