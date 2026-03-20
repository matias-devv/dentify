package com.dentify.domain.appointment.enums;


public enum AppointmentStatus {

    SCHEDULED,      // appointment created, patient has not yet arrived
                    // can be canceled, can be admitted

    CONFIRMED,      // the payment has already been made or patient confirmed attendance (48 hours prior)


    ADMITTED,       // patient arrived and was registered at reception
                    // requires valid payment according to policy

    IN_ATTENTION,   // the dentist has started the appointment
                    // explicit status to allow for actual delays

    CANCELLED_BY_SYSTEM,   // The system cancelled it due to lack of confirmation

    CANCELLED_BY_PATIENT,   // Patient reported absence

    CANCELLED_BY_DENTIST,

    CANCELLED_BY_SECRETARY,

    COMPLETED,      // medical care completed
                    // appointment closed, not editable

    NO_SHOW,        // patient did not show up (defined with a grace period)
                    // impacts patient metrics

    WALK_IN_PENDING;
                        // Patient marked as NO_SHOW who showed up anyway.
                        // Requires explicit staff action to proceed.
                        // The original slot is NOT modified in the calendar.
                        // The patient is waiting in an informal queue.

    public static AppointmentStatus fromString(String type) {
        try {
            return AppointmentStatus.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid appointment status: " + type);
        }
    }


}
