package com.dentify.exception.patient;

import com.dentify.exception.dto.AppException;

public class PatientAlreadyExistsException extends RuntimeException implements AppException {

    private final String errorCode = "PATIENT_ALREADY_EXISTS";

    public PatientAlreadyExistsException(String message) {
        super(message);
    }

    @Override
    public String getErrorCode() {
        return errorCode;
    }
}