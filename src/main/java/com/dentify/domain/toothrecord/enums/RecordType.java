package com.dentify.domain.toothrecord.enums;

/**
 * Classifies the nature of an odontogram record entry.
 * <p>
 * - PRE_EXISTING: pre-existing condition the patient already had
 * - REQUIRED:     treatment that needs to be performed
 * </p>
 */
public enum RecordType {
    PRE_EXISTING,
    REQUIRED
}