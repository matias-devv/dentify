package com.dentify.domain.exception.patient;

import com.dentify.domain.exception.dto.AppException;

public class PatientNotFoundException extends RuntimeException implements AppException {

    private final String errorCode = "PATIENT_NOT_FOUND";

    public PatientNotFoundException(String message) { super(message); }

    public String getErrorCode() { return errorCode; }
}
