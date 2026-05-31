package com.dentify.domain.medicalhistory.model;

import com.dentify.domain.complementaryexam.model.ComplementaryExam;
import com.dentify.domain.dentist.model.Dentist;
import com.dentify.domain.patient.model.Patient;
import com.dentify.domain.patientallergy.model.PatientAllergy;
import com.dentify.domain.toothrecord.enums.OdontogramType;
import com.dentify.domain.toothrecord.model.ToothRecord;
import com.dentify.domain.userProfile.model.UserProfile;
import com.dentify.security.multitenancy.TenantEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Represents one clinical evolution record for a patient.
 * A patient may have multiple HistorialClinico entries across time.
 * <p>
 * Replaces the deprecated {@code Diagnosticos} entity entirely.
 * </p>
 *
 * <b>Inherited from TenantEntity:</b> id, tenantId, clinic (clinic_id FK), createdAt, updatedAt.
 *
 * <b>Cascade rules:</b>
 * <ul>
 *   <li>toothRecords → CascadeType.ALL + orphanRemoval</li>
 *   <li>allergies          → CascadeType.ALL + orphanRemoval</li>
 *   <li>exams          → CascadeType.ALL + orphanRemoval</li>
 * </ul>
 *
 * <b>Business rule (enforced in service):</b>
 * When {@code noRefiereAlergias = true}, the {@code alergias} list must be empty.
 */

@Entity
@Table(name = "medical_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MedicalHistory extends TenantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ── Core fields ────────────────────────────────────────────────────────────

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    /**
     * Determines which FDI tooth ranges are valid for OdontogramRecord entries.
     * Validated in OdontogramRecordService.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "odontogram_type")
    private OdontogramType odontogramType;

    @Column(name = "path_medical_history", columnDefinition = "TEXT")
    private String pastMedicalHistory;

    @Column(name = "observations", columnDefinition = "TEXT")
    private String observations;

    /**
     * When true: patient reports no allergies.
     * must ensure the {allergies} list remains empty.
     */
    @Column(name = "no_allergies_reported", nullable = false)
    private Boolean noAllergiesReported = false;

    @Column(name = "daily_medication", columnDefinition = "TEXT")
    private String daily_Medication;

    // ── Many-to-one relationships ──────────────────────────────────────────────

    /**
     * Dentist who created / attends this clinical record. Required.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_dentist", nullable = false)
    private Dentist dentist;

    /**
     * The patient this record belongs to. Required.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_patient", nullable = false)
    private Patient patient;

    /**
     * Last UserProfile who edited this record. Nullable.
     * Updated on every PUT operation in the service.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "edited_by")
    private UserProfile editedBy;

    // ── One-to-many child collections ──────────────────────────────────────────

    @OneToMany(mappedBy = "medicalHistory", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ToothRecord> odontogramRecords;

    @OneToMany(mappedBy = "medicalHistory", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PatientAllergy> allergies;

    @OneToMany(mappedBy = "medicalHistory", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ComplementaryExam> exams;
}
