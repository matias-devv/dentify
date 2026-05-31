package com.dentify.domain.allergycatalog.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "allergy_catalog")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AllergyCatalog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "active", nullable = false)
    @Builder.Default
    private Boolean active = true;
}
