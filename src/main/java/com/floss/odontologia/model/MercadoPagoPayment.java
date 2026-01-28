package com.floss.odontologia.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity @AllArgsConstructor @NoArgsConstructor @Getter @Setter @Table ( name = "mercado_pago_payments")
public class MercadoPagoPayment {

    @Id @GeneratedValue ( strategy = GenerationType.IDENTITY)
    private Long id;

    @Column( length = 1000)
    private String payment_link; // the link provided by the dentist

    @Column( length = 100)
    private String preference_id; // identify the payment in MP (optional)

    @Column( length = 100)
    private String payment_id; // payment ID in MP upon completion

    private Integer installments; // number of installments (if applicable)

    private LocalDateTime generation_date;
    private LocalDateTime payment_date;

    @Column( precision = 10, scale = 2)
    private BigDecimal amount_paid;

    @Column( length = 500)
    private String receipt_url; // MP receipt URL

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_pay", nullable = false)
    private Pay pay;

}
