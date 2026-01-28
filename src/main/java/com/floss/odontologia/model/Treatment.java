package com.floss.odontologia.model;

import com.floss.odontologia.enums.TreatmentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Entity @AllArgsConstructor @NoArgsConstructor @Getter
@Setter @Table ( name = "treatments")
public class Treatment {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_treatment;

    private BigDecimal base_price;
    private Integer discount;
    private BigDecimal final_price;
    private BigDecimal outstanding_balance;

    @Enumerated(EnumType.STRING)
    private TreatmentStatus treatment_status;

    private LocalDate start_date;

    @JoinColumn (nullable = false)
    private LocalDate final_date;

    //n treatments -> one app_user
    @ManyToOne ( fetch = FetchType.LAZY)
    @JoinColumn ( name = "id_app_user", nullable = false)
    private AppUser app_user;

    //n treatments -> one product
    @ManyToOne ( fetch = FetchType.LAZY)
    @JoinColumn ( name = "id_product", nullable = true)
    private Product product;

    //n treatments -> one pack
    @ManyToOne ( fetch = FetchType.LAZY)
    @JoinColumn ( name = "id_pack", nullable = true)
    private PackProduct pack;

    //n treatments -> one patient
    @ManyToOne ( fetch = FetchType.LAZY)
    @JoinColumn ( name = "id_patient", nullable = false)
    private Patient patient;

    //one treatment -> n pagos
    @OneToMany ( mappedBy = "treatment")
    private List<Pay> pays;

    //one treatment -> n appointments
    @OneToMany ( mappedBy = "treatment")
    private List<Appointment> appointments;
}
