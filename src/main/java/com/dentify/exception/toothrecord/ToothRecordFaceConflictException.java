package com.dentify.exception.toothrecord;

import com.dentify.exception.dto.AppException;

public class ToothRecordFaceConflictException extends RuntimeException implements AppException {

    private final String errorCode = "TOOTH_RECORD_FACE_CONFLICT";

    public ToothRecordFaceConflictException(String message) {
        super(message);
    }

    @Override
    public String getErrorCode() {
        return errorCode;
    }
}
