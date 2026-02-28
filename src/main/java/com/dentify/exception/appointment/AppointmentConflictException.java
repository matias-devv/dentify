package com.dentify.domain.exception.appointment;

public class AppointmentConflictException extends RuntimeException {
    private final String errorCode = "APPOINTMENT_CONFLICT";
    public AppointmentConflictException(String message) { super(message); }
    public String getErrorCode() { return errorCode; }
}