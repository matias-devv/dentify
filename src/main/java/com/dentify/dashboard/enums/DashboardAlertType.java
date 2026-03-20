package com.dentify.dashboard.enums;

public enum DashboardAlertType {

    PAYMENT_PENDING, // Today's appointment with PENDING or AWAITMENT payment

    PARTIAL_PAYMENT, // Today's appointment with PARTIAL payment

    TREATMENT_ABANDONED, // Today's treatment is in ABANDONED status

    NO_SHOW_REGISTERED // Patient marked as NO_SHOW today

}
