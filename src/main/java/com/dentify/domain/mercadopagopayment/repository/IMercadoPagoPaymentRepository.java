package com.dentify.domain.mercadopagopayment.repository;

import com.dentify.domain.mercadopagopayment.model.MercadoPagoPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IMercadoPagoPaymentRepository extends JpaRepository< MercadoPagoPayment ,Long> {

    @Query("""
        SELECT DISTINCT mp
        FROM MercadoPagoPayment mp
        JOIN FETCH mp.pay pay
        JOIN FETCH pay.appointment app
        JOIN FETCH app.patient pat
        LEFT JOIN FETCH pat.patientResponsibleList pr
        LEFT JOIN FETCH pr.responsibleAdult
        JOIN FETCH app.dentist d
        JOIN FETCH d.userProfile up
        JOIN FETCH up.clinic
        JOIN FETCH up.authUser
        LEFT JOIN FETCH pay.treatment t
        LEFT JOIN FETCH t.product
        WHERE mp.externalReference = :externalRef
    """)
    Optional<MercadoPagoPayment> findByExternalReference(@Param("externalRef") String externalRef);
}
