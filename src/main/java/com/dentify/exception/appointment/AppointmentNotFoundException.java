package com.dentify.exception.appointment;

import com.dentify.exception.dto.AppException;

public class AppointmentNotFoundException extends RuntimeException implements AppException {
    private final String errorCode = "APPOINTMENT_NOT_FOUND";
    public AppointmentNotFoundException(String message) { super(message); }
    public String getErrorCode() { return errorCode; }
}