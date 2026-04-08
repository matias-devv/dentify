package com.dentify.exception.patient;

import com.dentify.exception.dto.AppException;

public class PatientEmailRequiredException extends RuntimeException implements AppException {

    private final String errorCode = "PATIENT_EMAIL_REQUIRED";

    public PatientEmailRequiredException(String message) {
        super(message);
    }

    @Override
    public String getErrorCode() {
        return errorCode;
    }
}