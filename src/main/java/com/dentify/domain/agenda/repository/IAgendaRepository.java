package com.dentify.domain.agenda.repository;

import com.dentify.domain.agenda.model.Agenda;
import com.dentify.domain.appointment.model.Appointment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface IAgendaRepository extends JpaRepository<Agenda, Long> {

    @Query("""
       SELECT a
       FROM Agenda a
       JOIN FETCH a.dentist d
       WHERE a.id_agenda = :agendaId
       """)
    Optional<Agenda> findAgendaWithDentistById(@Param("agendaId") Long agendaId);

    @Query("""
       SELECT DISTINCT a
       FROM Agenda a
       JOIN FETCH a.clinic c
       LEFT JOIN FETCH a.product p
       LEFT JOIN FETCH a.schedules s
       LEFT JOIN FETCH s.days d
       WHERE a.id_agenda = :id
       """)
    Optional<Agenda> findAgendaWithSchedules(@Param("id") Long id);

    @Query("""
        SELECT DISTINCT a
        FROM Agenda a
        LEFT JOIN FETCH a.schedules s
        WHERE a.dentist.id = :dentistId
        """)
    List<Agenda> findByDentistIdWithSchedules(@Param("dentistId") Long dentistId);

    @Query("SELECT a FROM Agenda a " +
            "JOIN FETCH a.dentist d " +
            "JOIN FETCH d.userProfile " +
            "LEFT JOIN FETCH a.product " +
            "LEFT JOIN FETCH a.schedules")
    List<Agenda> findAgendasByClinicId(@Param("clinicId") Long clinicId);
}
