package com.dentify.exception.diagnosistypecatalog;

import com.dentify.exception.dto.AppException;

public class DiagnosisTypeNotFoundException extends RuntimeException implements AppException {

    private final String errorCode = "DIAGNOSIS_TYPE_NOT_FOUND";

    public DiagnosisTypeNotFoundException(String message) {
        super(message);
    }

    @Override
    public String getErrorCode() {
        return errorCode;
    }
}