package com.dentify.exception.clinic;

import com.dentify.exception.dto.AppException;

public class ClinicNotFoundException extends RuntimeException implements AppException {

    private final String errorCode = "CLINIC_NOT_FOUND";

    public ClinicNotFoundException(String message) { super(message); }

    public String getErrorCode() { return errorCode; }
}