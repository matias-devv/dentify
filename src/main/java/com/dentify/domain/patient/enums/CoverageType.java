package com.dentify.domain.patient.enums;

import jakarta.validation.constraints.NotNull;

public enum CoverageType {

    SELF_PAY,          // No medical coverage
                       // Pays 100% of the treatment

    HEALTH_INSURANCE,  // Union/State coverage
                       // May involve future discounts

    PREPAID_INSURANCE, // Private coverage
                       // Prepared for agreements or co-payments

    OTHER              // Exceptional/unclassified cases
    ;

    public static boolean isInvalid(@NotNull CoverageType coverageType) {
        return !coverageType.equals(CoverageType.SELF_PAY) &&
               !coverageType.equals(CoverageType.HEALTH_INSURANCE) &&
               !coverageType.equals(CoverageType.PREPAID_INSURANCE) &&
               !coverageType.equals(CoverageType.OTHER) ;
    }
}
