package com.dentify.domain.toothrecord.enums;

/**
 * Defines which FDI notation set is valid for a given MedicalHistory.
 * <p>
 * - ADULT    : pieces 11–18, 21–28, 31–38, 41–48
 * - PEDIATRIC: pieces 51–55, 61–65, 71–75, 81–85
 * - MIX      : union of both ranges
 * </p>
 * Validated at service layer when persisting ToothRecord.
 */
public enum OdontogramType {
    ADULT,
    PEDIATRIC,
    MIX
}
