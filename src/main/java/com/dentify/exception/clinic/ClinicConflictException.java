package com.dentify.exception.clinic;

import com.dentify.exception.dto.AppException;

public class ClinicConflictException extends RuntimeException implements AppException {

    private final String errorCode = "CLINIC_CONFLICT_EXCEPTION";

    public ClinicConflictException(String message) {
        super(message);
    }

    @Override
    public String getErrorCode() {
        return errorCode;
    }
}
