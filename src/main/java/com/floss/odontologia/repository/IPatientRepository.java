package com.floss.odontologia.repository;

import com.floss.odontologia.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IPatientRepository extends JpaRepository<Patient,Long> {
}
