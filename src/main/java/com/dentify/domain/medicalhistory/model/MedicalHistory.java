package com.dentify.domain.medicalhistory.model;

import com.dentify.domain.complementaryexam.model.ComplementaryExam;
import com.dentify.domain.dentist.model.Dentist;
import com.dentify.domain.patient.model.Patient;
import com.dentify.domain.patientallergy.model.PatientAllergy;
import com.dentify.domain.toothrecord.enums.OdontogramType;
import com.dentify.domain.toothrecord.model.ToothRecord;
import com.dentify.domain.userProfile.model.UserProfile;
import com.dentify.exception.toothrecord.ToothRecordNotFoundException;
import com.dentify.security.multitenancy.TenantEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents one clinical evolution record for a patient.
 * A patient may have multiple MedicalHistory entries across time.
 *
 * <b>Inherited from TenantEntity:</b> id, tenantId, clinic (clinic_id FK).
 *
 * <b>Cascade rules:</b>
 * <ul>
 *   <li>toothRecords → CascadeType.ALL + orphanRemoval</li>
 *   <li>allergies          → CascadeType.ALL + orphanRemoval</li>
 *   <li>exams          → CascadeType.ALL + orphanRemoval</li>
 * </ul>
 *
 * <b>Business rule (enforced in service):</b>
 * When {@code hasAllergies = false}, the {@code allergies} list must be empty.
 */

@Entity
@Table(name = "medical_histories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
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

    @Column(name = "past_medical_history", columnDefinition = "TEXT")
    private String pastMedicalHistory;

    @Column(name = "observations", columnDefinition = "TEXT")
    private String observations;

    /**
     * When true: patient reports no allergies.
     * must ensure the {allergies} list remains empty.
     */
    @Column(name = "has_allergies", nullable = false)
    private Boolean hasAllergies = false;

    @Column(name = "daily_medication", columnDefinition = "TEXT")
    private String dailyMedication;

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
    private List<ToothRecord> toothRecords = new ArrayList<>();

    @OneToMany(mappedBy = "medicalHistory", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PatientAllergy> allergies;

    @OneToMany(mappedBy = "medicalHistory", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ComplementaryExam> exams;

    public void addToothRecords(List<ToothRecord> toothRecords) {

        if ( toothRecords.isEmpty() ) return;

        if ( this.toothRecords == null ) {
            this.toothRecords = new ArrayList<>();
        }

        this.toothRecords.addAll( toothRecords );
    }


    public void addAllergies(List<PatientAllergy> allergies) {

        if ( allergies.isEmpty() ) return;

        if( this.allergies == null || this.allergies.isEmpty() ) {
            setAllergies(allergies);
        }
        else{
            //I chose "add" instead of "addAll" because otherwise it would overwrite the other previously saved allergies with the new ones.
            allergies.forEach( a -> this.allergies.add( a ) );
        }
    }

    public boolean isToothRecordsListEmpty() {
        return this.toothRecords == null || this.toothRecords.isEmpty();
    }

    public boolean isAllergiesListEmpty() {
        return this.allergies == null || this.allergies.isEmpty();
    }

    public boolean isOdontogramTypeNull() {

        if ( this.getOdontogramType() == null ) {
            return true;
        } else {
            return false;
        }
    }

    public ToothRecord getToothRecordOrThrow(Long toothRecordId) {
        return toothRecords.stream()
                           .filter(tr -> tr.getId().equals( toothRecordId ) )
                           .findFirst()
                           .orElseThrow(() -> new ToothRecordNotFoundException( "The tooth record with id " + toothRecordId + " was not found" ) );
    }

    public void addExam(ComplementaryExam exam) {

        if ( this.exams == null ) {
            this.exams = new ArrayList<>();
        }

        this.exams.add( exam );
    }
}
