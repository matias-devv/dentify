package com.dentify.domain.diagnosistypecatalog.model;

import com.dentify.domain.clinic.model.Clinic;
import com.dentify.domain.diagnosistypecatalog.enums.DiagnosisSymbol;
import jakarta.persistence.*;
import lombok.*;

/**
 * Catalog of diagnosis types used in the odontogram.
 * <p>
 * Two kinds of records coexist in this table:
 * <ul>
 *   <li><b>Global (esGlobal = true)</b>: Predefined by the system, seeded at startup.
 *       {@code clinic} is null. Visible to every clinic.</li>
 *   <li><b>Custom (esGlobal = false)</b>: Created per-clinic.
 *       {@code clinic} references the owning Clinic. Visible only to that clinic.</li>
 * </ul>
 * Does not extend TenantEntity because clinic_id is intentionally nullable
 * for global records.
 * </p>
 */
@Entity
@Table(name = "diagnosis_type_catalog")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiagnosisTypeCatalog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Display name shown in the odontogram reference panel. */
    @Column(name = "name", nullable = false)
    private String name;

    /** Symbol type used to render the glyph on the odontogram canvas. */
    @Enumerated(EnumType.STRING)
    @Column(name = "simbol")
    private DiagnosisSymbol simbol;

    /** True = system predefined; false = clinic custom. */
    @Column(name = "is_global", nullable = false)
    private Boolean isGlobal;

    @Column(name = "active", nullable = false)
    private Boolean active = true;

    /**
     * Null when esGlobal = true.
     * References the owning Clinic when this is a custom (clinic-specific) entry.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "clinic_id")
    private Clinic clinic;
}

