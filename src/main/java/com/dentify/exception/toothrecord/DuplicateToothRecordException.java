package com.dentify.exception.toothrecord;

import com.dentify.exception.dto.AppException;

public class DuplicateToothRecordException extends RuntimeException implements AppException {

    private final String errorCode = "DUPLICATE_TOOTH_RECORD";

    public DuplicateToothRecordException(String message) {
        super(message);
    }

    @Override
    public String getErrorCode() {
        return errorCode;
    }
}
