package com.dentify.exception.clinic;

import com.dentify.exception.dto.AppException;

public class ClinicCuitAlreadyExistsException extends RuntimeException implements AppException {

    private final String errorCode = "CLINIC_CUIT_ALREADY_EXISTS";

    public ClinicCuitAlreadyExistsException(String message) {
        super(message);
    }

    @Override
    public String getErrorCode() {
        return errorCode;
    }
}