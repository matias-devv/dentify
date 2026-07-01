package com.dentify.exception.medicalhistory;

import com.dentify.exception.dto.AppException;

public class OdontogramTypeConflictException extends RuntimeException implements AppException {

    private final String errorCode = "ODONTOGRAM_TYPE_LOCKED";

    public OdontogramTypeConflictException(String message) {
        super(message);
    }

    @Override
    public String getErrorCode() {
        return errorCode;
    }
}
