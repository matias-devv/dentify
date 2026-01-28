package com.floss.odontologia.enums;

public enum TreatmentStatus {

    // Initial states
    PAYMENT_PENDING,
    PARTIAL_PAYMENT,

    // States during treatment
    SCHEDULED,
    IN_PROGRESS,
    PAUSED,
    RESCHEDULED,

    // Final states
    COMPLETED,
    PATIENT_CANCELLED,
    FACILITY_CANCELLED,
    SYSTEM_CANCELLED,

    // Follow-up states
    FOLLOW_UP,
    EVALUATION
}
