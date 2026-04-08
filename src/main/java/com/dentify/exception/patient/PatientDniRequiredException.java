package com.dentify.exception.patient;

import com.dentify.exception.dto.AppException;

public class PatientDniRequiredException extends RuntimeException implements AppException {

    private final String errorCode = "PATIENT_DNI_REQUIRED";

    public PatientDniRequiredException(String message) {
        super(message);
    }

    @Override
    public String getErrorCode() {
        return errorCode;
    }
}