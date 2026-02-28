package com.dentify.domain.exception.appointment;

public class AppointmentStateException extends RuntimeException {
    private final String errorCode = "INVALID_APPOINTMENT_STATE";
    public AppointmentStateException(String message) { super(message); }
    public String getErrorCode() { return errorCode; }
}