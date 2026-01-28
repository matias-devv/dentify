package com.floss.odontologia.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalTime;
import java.util.List;

@Entity @AllArgsConstructor @NoArgsConstructor @Getter @Setter
@Table ( name = "products")
public class Product {

    @Id @GeneratedValue ( strategy = GenerationType.IDENTITY)
    private Long id_product;

    private String name_product;
    private BigDecimal unit_price;
    private String description;
    private Boolean activo;

    @Column(name = "duration_minutes", nullable = false)
    private Integer duration_minutes;

    @OneToMany( mappedBy = "product")
    private List<Agenda> agendas;

    @ManyToOne
    @JoinColumn( name = "id_speciality")
    private Speciality speciality;

    @OneToMany ( mappedBy = "product" )
    private List<Treatment> treatments;
}
