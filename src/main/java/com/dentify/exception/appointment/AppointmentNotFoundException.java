package com.dentify.domain.exception.appointment;

public class AppointmentNotFoundException extends RuntimeException {
    private final String errorCode = "APPOINTMENT_NOT_FOUND";
    public AppointmentNotFoundException(String message) { super(message); }
    public String getErrorCode() { return errorCode; }
}