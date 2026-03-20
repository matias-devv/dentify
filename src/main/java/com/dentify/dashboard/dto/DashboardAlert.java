package com.dentify.dashboard.dto;

import com.dentify.dashboard.enums.DashboardAlertType;

public record DashboardAlert(DashboardAlertType type,
                            Long reference_id,   // id of the payment or the appointment
                            String patient_name,
                            String patient_surname,
                            String hora,
                            String message) { }