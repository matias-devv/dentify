package com.dentify.exception.toothrecord;

import com.dentify.exception.dto.AppException;

public class MissingOdontogramTypeException extends RuntimeException implements AppException {

    private final String errorCode = "MISSING_ODONTOGRAM_TYPE";

    public MissingOdontogramTypeException(String message) {
        super(message);
    }

    @Override
    public String getErrorCode() {
        return errorCode;
    }
}
