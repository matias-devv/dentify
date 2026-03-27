package com.dentify.domain.product.model;

import com.dentify.domain.agenda.model.Agenda;
import com.dentify.domain.clinic.model.Clinic;
import com.dentify.domain.speciality.model.Speciality;
import com.dentify.domain.treatment.model.Treatment;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Table(name = "products", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"name_product", "clinic_id"})
})
@Entity @AllArgsConstructor @NoArgsConstructor @Getter @Setter @Builder
public class Product {

    @Id @GeneratedValue ( strategy = GenerationType.IDENTITY)
    private Long id_product;

    @ManyToOne
    @JoinColumn( name = "id_speciality")
    private Speciality speciality;

    @Column(name = "name_product", nullable = false)
    private String nameProduct;

    @Column(name = "unit_price")
    private BigDecimal unitPrice;

    private String description;
    private Boolean active;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "clinic_id", nullable = false)
    private Clinic clinic;

    @OneToMany( mappedBy = "product")
    private List<Agenda> agendas;

    @OneToMany ( mappedBy = "product" )
    private List<Treatment> treatments;
}
