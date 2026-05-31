package com.dentify.domain.diagnosistypecatalog.enums;

/**
 * Symbols rendered on the interactive odontogram UI.
 * Each value maps to a visual glyph drawn per tooth/face.
 * <p>
 * Symbols reference (UI rendering side):
 * ROOT_CANAL_TREATMENT  → TC
 * INCURABLE_TOOTH_DECAY → ■
 * MISSING_TOOTH         → X
 * SILICATE_FILLING      → /S
 * PARADENTOSIS          → Pd
 * PERNO                 → P
 * BRIDGE                → ⊓
 * ORTHODONTICS          → 〰
 * TREATABLE_DECAY       → ●
 * EXTRACTION            → =
 * AMALGAM_FILLING       → /A
 * ACRYLIC_FILLING       → /Ac
 * CROWN                 → ○
 * INLAY_ONLAY           → |
 * REMOVABLE_PROSTHESIS  → ☐
 * IMPLANT               → IM
 * CUSTOM                → clinic-defined, no standard glyph
 * </p>
 */
public enum DiagnosisSymbol {
    ROOT_CANAL_TREATMENT,
    INCURABLE_TOOTH_DECAY,
    MISSING_TOOTH,
    SILICATE_FILLING,
    PARADENTOSIS,
    PERNO,
    BRIDGE,
    ORTHODONTICS,
    TREATABLE_DECAY,
    EXTRACTION,
    AMALGAM_FILLING,
    ACRYLIC_FILLING,
    CROWN,
    INLAY_ONLAY,
    REMOVABLE_PROSTHESIS,
    IMPLANT,
    CUSTOM
}
