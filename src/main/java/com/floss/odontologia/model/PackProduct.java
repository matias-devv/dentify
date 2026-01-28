package com.floss.odontologia.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Entity @AllArgsConstructor @NoArgsConstructor @Getter @Setter @Table ( name = "pack_products")
public class PackProduct {

    @Id @GeneratedValue ( strategy = GenerationType.IDENTITY)
    private Long id_pack_product;

    private String name_pack;
    private LocalDate date_creation;
    private BigDecimal total_price;
    private BigDecimal price_without_discount;
    private String description;
    private Boolean active;

    @Column(name = "duration_minutes", nullable = true)
    private Integer duration_minutes;
    @Column( nullable = true)
    private Integer discount;

    @Column(nullable = false)
    List<Long> products_ids;

    @OneToMany ( mappedBy = "pack" )
    private List<Treatment> treatments;

}
