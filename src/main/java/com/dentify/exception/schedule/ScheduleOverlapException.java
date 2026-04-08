package com.dentify.exception.schedule;


import com.dentify.exception.dto.AppException;

public class ScheduleOverlapException extends RuntimeException implements AppException {

    private final String errorCode = "SCHEDULE_OVERLAP";

    public ScheduleOverlapException(String message) {
        super(message);
    }

    @Override
    public String getErrorCode() {
        return errorCode;
    }
}
