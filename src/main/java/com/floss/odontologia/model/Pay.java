package com.floss.odontologia.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity @Getter @Setter @AllArgsConstructor @NoArgsConstructor @Table ( name = "pays")
public class Pay {

    @Id @GeneratedValue ( strategy = GenerationType.IDENTITY)
    private Long id_pay;

    private BigDecimal amount;

    @Enumerated ( EnumType.STRING )
    private PaymentMethod payment_method;

    @Enumerated ( EnumType.STRING)
    private PaymentStatus payment_status;

    private LocalDate date_generation;
    @Column(nullable = true)
    private LocalDate payment_date;

    @OneToOne ( mappedBy = "pay")
    private PaymentReceipt payment_receipt;

    @OneToOne ( mappedBy = "pay")
    private Notification notification;

    @ManyToOne ( fetch = FetchType.LAZY)
    @JoinColumn ( name = "id_appointment", nullable = true)
    private Appointment appointment;

    @ManyToOne ( fetch = FetchType.EAGER)
    @JoinColumn ( name = "id_treatment", nullable = false)
    private Treatment treatment;

    @OneToOne ( mappedBy = "pay")
    private MercadoPagoPayment mercado_pago_payment;

}
