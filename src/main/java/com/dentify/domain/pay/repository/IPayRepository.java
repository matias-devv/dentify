package com.dentify.domain.pay.repository;

import com.dentify.domain.appointment.enums.AppointmentStatus;
import com.dentify.domain.pay.enums.PaymentStatus;
import com.dentify.domain.pay.model.Pay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface IPayRepository extends JpaRepository<Pay, Long> {

    @Query("""
    SELECT COALESCE(SUM(p.amount), 0)
    FROM Pay p
    WHERE p.payment_status = :status
    AND p.date_generation BETWEEN :startOfDay AND :endOfDay
    """)
    BigDecimal getDailyIncome(@Param("status") PaymentStatus status,
                              @Param("startOfDay") LocalDateTime startOfDay,
                              @Param("endOfDay") LocalDateTime endOfDay);

    @Query("""
        SELECT COALESCE(SUM(p.amount), 0)
        FROM Pay p
        WHERE p.payment_status = :status
        AND p.date_generation BETWEEN :startOfMonth AND :endOfMonth
    """)
    BigDecimal getMonthlyIncome(@Param("status") PaymentStatus status,
                                @Param("startOfMonth") LocalDateTime startOfMonth,
                                @Param("endOfMonth") LocalDateTime endOfMonth);

    @Query("""
        SELECT DISTINCT p
        FROM Pay p
        JOIN FETCH p.appointment a
        JOIN FETCH a.patient pat
        JOIN FETCH p.treatment t
        LEFT JOIN FETCH t.product prod
        LEFT JOIN FETCH p.payment_receipt r
        WHERE a.dentist.id = :dentistId
    """)
    List<Pay> findPaysByDentistId(@Param("dentistId") Long dentistId);

    @Query("""
        SELECT DISTINCT p
        FROM Pay p
        JOIN FETCH p.appointment a
        JOIN FETCH a.patient pat
        LEFT JOIN FETCH p.treatment t
        WHERE a.dentist.id = :dentistId
        AND a.appointmentDate BETWEEN :startOfDay AND :endOfDay
        AND a.appointmentStatus NOT IN :excludedStatuses
        ORDER BY a.appointmentDate ASC
    """)
    List<Pay> findPaymentsToday(@Param("dentistId") Long dentistId, @Param("startOfDay") LocalDateTime startOfDay,
                                @Param("endOfDay") LocalDateTime endOfDay, @Param("excludedStatuses") List<AppointmentStatus> excludedStatuses);

    @Query("""
        SELECT DISTINCT p
        FROM Pay p
        LEFT JOIN FETCH p.appointment a
        LEFT JOIN FETCH a.patient pat
        LEFT JOIN FETCH pat.patientResponsibleList pr
        LEFT JOIN FETCH pr.responsibleAdult
        LEFT JOIN FETCH a.dentist d
        LEFT JOIN FETCH d.userProfile up
        LEFT JOIN FETCH up.authUser au
        LEFT JOIN FETCH up.clinic c
        LEFT JOIN FETCH a.treatment t
        LEFT JOIN FETCH t.product prod
        WHERE p.id_pay = :payId
    """)
    Optional<Pay> findByIdWithFullGraph(@Param("payId") Long payId);

}
