package com.dentify.domain.medicalhistory.repository;

import com.dentify.domain.medicalhistory.model.MedicalHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IMedicalHistoryRepository extends JpaRepository<MedicalHistory, Long> {
}
