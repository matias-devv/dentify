package com.dentify.exception.appointment;

import com.dentify.exception.dto.AppException;

public class AppointmentConflictException extends RuntimeException  implements AppException {
    private final String errorCode = "APPOINTMENT_CONFLICT";
    public AppointmentConflictException(String message) { super(message); }
    public String getErrorCode() { return errorCode; }
}