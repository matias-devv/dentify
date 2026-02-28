package com.dentify.domain.exception.dentist;

import com.dentify.domain.exception.dto.AppException;

public class DentistNotFoundException extends RuntimeException implements AppException {
    private final String errorCode = "DENTIST_NOT_FOUND";
    public DentistNotFoundException(String message) { super(message); }
    public String getErrorCode() { return errorCode; }
}