package com.dentify.exception.appointment;

import com.dentify.exception.dto.AppException;

public class AppointmentStateException extends RuntimeException implements AppException {
    private final String errorCode = "INVALID_APPOINTMENT_STATE";
    public AppointmentStateException(String message) { super(message); }
    public String getErrorCode() { return errorCode; }
}