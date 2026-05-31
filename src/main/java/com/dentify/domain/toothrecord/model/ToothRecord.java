package com.dentify.domain.toothrecord.model;

import com.dentify.domain.diagnosistypecatalog.model.DiagnosisTypeCatalog;
import com.dentify.domain.medicalhistory.model.MedicalHistory;
import com.dentify.domain.toothrecord.enums.RecordType;
import com.dentify.domain.toothrecord.enums.ToothFace;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * A single diagnosis record for one tooth face a clinical history.
 * <p>
 * Multiple records may reference the same {@code pieceNumber} (e.g. different faces).
 * Bulk creation is handled at service level: the endpoint accepts a list and
 * persists one OdontogramRecord per entry.
 * </p>
 *
 * <b>pieceNumber validation (service-enforced based on parent odontogramType):</b>
 * <ul>
 *   <li>ADULT    → 11–18, 21–28, 31–38, 41–48</li>
 *   <li>PEDIATRIC→ 51–55, 61–65, 71–75, 81–85</li>
 *   <li>MIX      → union of both ranges</li>
 * </ul>
 *
 * Intentionally immutable after creation — edits are performed via delete + re-create.
 * Does not extend TenantEntity; tenant scope is reached via {@code MedicalHistory}.
 */
@Entity
@Table(name = "tooth_record")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ToothRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * FDI tooth number (international two-digit notation).
     * Validation against the parent odontogramType is enforced in the service layer.
     */
    @Column(name = "piece_number", nullable = false)
    private int pieceNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "record_type")
    private RecordType recordType;

    @Enumerated(EnumType.STRING)
    @Column(name = "face")
    private ToothFace face;

    @Column(name = "observations", columnDefinition = "TEXT")
    private String observations;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // ── Relationships ──────────────────────────────────────────────────────────

    /** Parent clinical history. Deletion cascades from MedicalHistory. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medical_history_id", nullable = false)
    private MedicalHistory medicalHistory;

    /**
     * Diagnosis type from catalog.
     * Maybe a global entry (isGlobal = true) or a clinic-custom entry.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "diagnosis_id", nullable = false)
    private DiagnosisTypeCatalog diagnosisType;
}
