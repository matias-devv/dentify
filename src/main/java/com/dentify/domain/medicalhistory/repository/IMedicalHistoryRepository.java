package com.dentify.domain.medicalhistory.repository;

import com.dentify.domain.medicalhistory.model.MedicalHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IMedicalHistoryRepository extends JpaRepository<MedicalHistory, Long> {

    @Query("""
        SELECT mh FROM MedicalHistory mh
        JOIN FETCH mh.dentist d
        JOIN FETCH d.userProfile dp
        LEFT JOIN FETCH mh.editedBy eb
        WHERE mh.patient.id = :patientId
        ORDER BY mh.startDate DESC
        """)
    List<MedicalHistory> findAllByPatientIdOrderByStartDateDesc(@Param("patientId") Long patientId);

    @Query("SELECT COUNT(tr) FROM ToothRecord tr WHERE tr.medicalHistory.id = :medicalHistoryId")
    int countToothRecordsByMedicalHistoryId(@Param("medicalHistoryId") Long medicalHistoryId);

    @Query("SELECT COUNT(pa) FROM PatientAllergy pa WHERE pa.medicalHistory.id = :medicalHistoryId")
    int countAllergiesByMedicalHistoryId(@Param("medicalHistoryId") Long medicalHistoryId);

    @Query("SELECT COUNT(ce) FROM ComplementaryExam ce WHERE ce.medicalHistory.id = :medicalHistoryId")
    int countExamsByMedicalHistoryId(@Param("medicalHistoryId") Long medicalHistoryId);

    @Query("""
    SELECT mh FROM MedicalHistory mh
    LEFT JOIN FETCH mh.patient
    LEFT JOIN FETCH mh.dentist d
    LEFT JOIN FETCH d.userProfile
    LEFT JOIN FETCH mh.editedBy
    WHERE mh.id = :id
    """)
    Optional<MedicalHistory> findMedicalHistoryBaseById(@Param("id") Long id);

    @Query("""
    SELECT mh FROM MedicalHistory mh
    LEFT JOIN FETCH mh.toothRecords tr
    LEFT JOIN FETCH tr.diagnosisType
    WHERE mh.id = :id
    """)
    Optional<MedicalHistory> findWithToothRecords(@Param("id") Long id);

    @Query("""
    SELECT mh FROM MedicalHistory mh
    LEFT JOIN FETCH mh.allergies a
    LEFT JOIN FETCH a.allergy
    WHERE mh.id = :id
    """)
    Optional<MedicalHistory> findWithAllergies(@Param("id") Long id);

    @Query("""
    SELECT mh FROM MedicalHistory mh
    LEFT JOIN FETCH mh.exams e
    LEFT JOIN FETCH e.uploadBy
    WHERE mh.id = :id
    """)
    Optional<MedicalHistory> findWithExams(@Param("id") Long id);
}
