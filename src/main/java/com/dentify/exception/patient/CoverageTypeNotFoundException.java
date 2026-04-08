package com.dentify.exception.patient;

import com.dentify.exception.dto.AppException;

public class CoverageTypeNotFoundException extends RuntimeException implements AppException {

    private final String errorCode = "COVERAGE_TYPE_NOT_FOUND";

    public CoverageTypeNotFoundException(String message) {
        super(message);
    }

    @Override
    public String getErrorCode() {
        return errorCode;
    }
}