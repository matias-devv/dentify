package com.dentify.domain.patientallergy.model;

import com.dentify.domain.allergycatalog.model.AllergyCatalog;
import com.dentify.domain.medicalhistory.model.MedicalHistory;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Intermediate table
 *
 * Links a medical history to a catalogued allergy.
 * <p>
 * Service rule: records in this table must not exist when the parent
 * {@code MedicalHistory.noReportAllergies} is {@code true}.
 * </p>
 * Does not extend TenantEntity; tenant scope is reached via {@code MedicalHistory}.
 */
@Entity
@Table(name = "patient_allergy")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PatientAllergy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Optional clinical note specific to this allergy for this patient. */
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    // ── Relationships ──────────────────────────────────────────────────────────

    /** Parent clinical history. Deletion cascades from MedicalHistory. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medical_history_id", nullable = false)
    private MedicalHistory medicalHistory;

    /** The allergy from the global catalog. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_allergy", nullable = false)
    private AllergyCatalog allergy;
}

