package com.dentify.exception.patient;

import com.dentify.exception.dto.AppException;

public class PatientNotFoundException extends RuntimeException implements AppException {

    private final String errorCode = "PATIENT_NOT_FOUND";

    public PatientNotFoundException(String message) { super(message); }

    public String getErrorCode() { return errorCode; }
}
