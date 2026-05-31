package com.dentify.domain.toothrecord.enums;

/**
 * Represents which face of the tooth the diagnosis applies to.
 * WHOLE_TOOTH is used when the condition affects the whole tooth
 * (e.g. MISSING_TOOTH, CROWN, IMPLANT).
 */
public enum ToothFace {
    VESTIBULAR,
    PALATAL,
    DISTAL,
    MESIAL,
    INCISAL,
    WHOLE_TOOTH
}