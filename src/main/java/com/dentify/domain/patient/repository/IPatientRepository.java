package com.dentify.domain.patient.repository;

import com.dentify.domain.patient.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IPatientRepository extends JpaRepository<Patient, Long> {

    @Query("""
       SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END
       FROM Patient p
       WHERE p.dni = :dni
       """)
    boolean existsByDni(@Param("dni") String dni);

    @Query("""
    SELECT p
    FROM Patient p
    WHERE p.clinic.id = :clinicId
    """)
    List<Patient> findAllByClinicId(@Param("clinicId") Long clinicId);

    @Query("""
       SELECT p
       FROM Patient p
       WHERE p.id_patient = :patientId
       AND p.clinic.id = :clinicId
       """)
    Optional<Patient> findPatientByIdAndClinicId(@Param("patientId") Long patientId, @Param("clinicId") Long clinicId );
}
