package com.dentify.domain.appointment.repository;

import com.dentify.domain.appointment.enums.AppointmentStatus;
import com.dentify.domain.appointment.model.Appointment;
import com.dentify.domain.treatment.enums.TreatmentStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Pageable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface IAppointmentRepository extends JpaRepository<Appointment, Long> {

    @Query("""
        SELECT DISTINCT a FROM Appointment a
        JOIN FETCH a.patient pat
        LEFT JOIN FETCH pat.patientResponsibleList pr
        LEFT JOIN FETCH pr.responsibleAdult
        JOIN FETCH a.dentist d
        JOIN FETCH d.userProfile up
        JOIN FETCH up.clinic
        JOIN FETCH up.authUser
        LEFT JOIN FETCH a.treatment t
        LEFT JOIN FETCH t.product
        LEFT JOIN FETCH a.pays p
        LEFT JOIN FETCH p.mercado_pago_data
        WHERE a.appointmentStatus IN :statuses
        AND a.appointmentDate BETWEEN :start AND :end
    """)
    List<Appointment> findScheduledAppointmentWithDetails(@Param("statuses") List<AppointmentStatus> statuses, @Param("start") LocalDateTime start,
                                                          @Param("end") LocalDateTime end);

    @Query("""
        SELECT DISTINCT a FROM Appointment a
        JOIN FETCH a.patient pat
        LEFT JOIN FETCH pat.patientResponsibleList pr
        LEFT JOIN FETCH pr.responsibleAdult
        JOIN FETCH a.dentist d
        JOIN FETCH d.userProfile up
        JOIN FETCH up.clinic
        JOIN FETCH up.authUser
        LEFT JOIN FETCH a.treatment t
        LEFT JOIN FETCH t.product
        LEFT JOIN FETCH a.pays p
        LEFT JOIN FETCH p.mercado_pago_data
        WHERE a.appointmentStatus = :status
        AND a.attendanceConfirmed = :confirmed
        AND a.appointmentDate BETWEEN :start AND :end
    """)
    List<Appointment> findReservedAppointmentsNotConfirmedWithDetails(@Param("status") AppointmentStatus status, @Param("confirmed") Boolean confirmed,
                                                                          @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("""
        SELECT DISTINCT a FROM Appointment a
        JOIN FETCH a.patient pat
        LEFT JOIN FETCH pat.patientResponsibleList pr
        LEFT JOIN FETCH pr.responsibleAdult
        JOIN FETCH a.dentist d
        JOIN FETCH d.userProfile up
        JOIN FETCH up.clinic
        JOIN FETCH up.authUser
        LEFT JOIN FETCH a.treatment t
        LEFT JOIN FETCH t.product
        LEFT JOIN FETCH a.pays p
        LEFT JOIN FETCH p.mercado_pago_data
        WHERE a.appointmentDate <= :end
        AND a.appointmentStatus IN :statuses
    """)
    List<Appointment> findByDateLessThanEqualAndAppointmentStatusInWithDetails(@Param("end") LocalDateTime end, @Param("statuses") List<AppointmentStatus> statuses);

    @Query("SELECT app FROM Appointment app " +
            "JOIN FETCH app.patient " +
            "JOIN FETCH app.dentist d " +
            "JOIN FETCH d.userProfile " +
            "LEFT JOIN FETCH app.treatment t " +
            "LEFT JOIN FETCH t.product " +
            "WHERE app.agenda.id_agenda = :agendaId " +
            "AND app.appointmentDate BETWEEN :start AND :end" )
    List<Appointment> findAppointmentsByAgendaAndDateRange(@Param("agendaId") Long agendaId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT app FROM Appointment app " +
            "JOIN FETCH app.patient " +
            "LEFT JOIN FETCH app.treatment t " +
            "LEFT JOIN FETCH t.product " +
            "WHERE app.agenda.id_agenda = :agendaId " +
            "AND CAST(app.appointmentDate AS date) = :date")
    List<Appointment> findAppointmentsByAgendaAndDate( @Param("agendaId") Long agendaId, @Param("date") LocalDate date );

    @Query("SELECT DISTINCT app FROM Appointment app " +
            "JOIN FETCH app.patient " +
            "JOIN FETCH app.dentist d " +
            "JOIN FETCH d.userProfile up " +
            "LEFT JOIN FETCH d.specialities " +
            "JOIN FETCH app.agenda " +
            "LEFT JOIN FETCH app.treatment t " +
            "LEFT JOIN FETCH t.product " +
            "LEFT JOIN FETCH app.pays " +
            "WHERE app.id_appointment = :id")
    Optional<Appointment> findByIdWithAllDetails(@Param("id") Long id);

    @Query("SELECT DISTINCT app FROM Appointment app " +
            "JOIN FETCH app.patient pat " +
            "LEFT JOIN FETCH pat.patientResponsibleList pr " +
            "LEFT JOIN FETCH pr.responsibleAdult " +
            "JOIN FETCH app.dentist d " +
            "JOIN FETCH d.userProfile up " +
            "JOIN FETCH up.clinic " +
            "JOIN FETCH up.authUser " +
            "LEFT JOIN FETCH app.treatment t " +
            "LEFT JOIN FETCH t.product " +
            "WHERE app.id_appointment = :id")
    Optional<Appointment> findByIdForCancellation(@Param("id") Long id);

    @Query("""
        SELECT COUNT(a)
        FROM Appointment a
        WHERE CAST(a.appointmentDate AS date) = :date
          AND a.appointmentStatus NOT IN :statuses
    """)
    Long countAppointmentsTodayExcludingStatuses( @Param("date") LocalDate date, @Param("statuses") List<AppointmentStatus> statuses );


    @Query("""
    SELECT a
    FROM Appointment a
    JOIN FETCH a.patient p
    WHERE a.appointmentDate >= :currentDateTime
      AND a.appointmentStatus IN :statuses
    ORDER BY a.appointmentDate ASC
    """)
    List<Appointment> findNextAppointment( @Param("currentDateTime") LocalDateTime currentDateTime, @Param("statuses") List<AppointmentStatus> statuses,
                                           Pageable pageable);

    @EntityGraph(attributePaths = {"pays", "treatment"})
    @Query("""
       SELECT a
       FROM Appointment a
       WHERE a.id_appointment = :id
       """)
    Optional<Appointment> findByIdWithPays(@Param("id") Long id);

    @Query("SELECT a FROM Appointment a WHERE CAST(a.appointmentDate AS date) = :date AND a.dentist.id = :dentistId AND a.appointmentStatus NOT IN :cancelledStatuses")
    List<Appointment> findByDateAndDentistId(@Param("date") LocalDate date, @Param("dentistId") Long dentistId);


    @Query("""
        SELECT a
        FROM Appointment a
        JOIN FETCH a.patient p
        LEFT JOIN FETCH a.treatment t
        LEFT JOIN FETCH t.product
        WHERE a.dentist.id = :dentistId
          AND a.appointmentDate BETWEEN :startOfDay AND :endOfDay
          AND a.appointmentStatus IN :statuses
        ORDER BY a.appointmentDate ASC
    """)
    List<Appointment> findDentistAppointmentsForDayWithDetails(@Param("dentistId") Long dentistId, @Param("startOfDay") LocalDateTime startOfDay,
                                                               @Param("endOfDay") LocalDateTime endOfDay, @Param("statuses") List<AppointmentStatus> statuses );

    @Query("""
        SELECT a
        FROM Appointment a
        JOIN FETCH a.patient p
        LEFT JOIN FETCH a.treatment t
        LEFT JOIN FETCH t.product
        WHERE a.id_appointment = :id
    """)
    Optional<Appointment> findByIdWithPatientAndTreatment(@Param("id") Long id);

    @Query("""
        SELECT DISTINCT a
        FROM Appointment a
        JOIN FETCH a.patient p
        LEFT JOIN FETCH p.patient_stat ps
        LEFT JOIN FETCH a.treatment t
        WHERE a.dentist.id = :dentistId
        AND a.appointmentDate BETWEEN :startOfDay AND :endOfDay
        AND (
            a.appointmentStatus = :appointmentStatus
            OR t.treatmentStatus = :treatmentStatus
        )
        ORDER BY a.appointmentDate ASC
    """)
    List<Appointment> findAppointmentAlertsToday(@Param("dentistId") Long dentistId, @Param("startOfDay") LocalDateTime startOfDay,
                                                 @Param("endOfDay") LocalDateTime endOfDay, @Param("appointmentStatus") AppointmentStatus appointmentStatus,
                                                 @Param("treatmentStatus") TreatmentStatus treatmentStatus);

}
