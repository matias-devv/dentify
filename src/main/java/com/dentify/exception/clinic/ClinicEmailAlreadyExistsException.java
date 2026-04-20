package com.dentify.exception.clinic;

import com.dentify.exception.dto.AppException;

public class ClinicEmailAlreadyExistsException extends RuntimeException implements AppException {

    private final String errorCode = "CLINIC_EMAIL_ALREADY_EXISTS";

    public ClinicEmailAlreadyExistsException(String message) {
        super(message);
    }

    @Override
    public String getErrorCode() {
        return errorCode;
    }
}