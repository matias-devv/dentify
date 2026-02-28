package com.dentify.domain.exception.schedule;

import com.dentify.domain.exception.dto.AppException;

public class InvalidScheduleException extends RuntimeException implements AppException {
    private final String errorCode = "INVALID_SCHEDULE";
    public InvalidScheduleException(String message) { super(message); }
    public String getErrorCode() { return errorCode; }
}
