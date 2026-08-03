package com.dentify.exception.general;

import com.dentify.exception.dto.AppException;

public class EmptyFileException extends RuntimeException implements AppException {

    private final String errorCode = "EMPTY_FILE_EXCEPTION";

    public EmptyFileException(String message) {
        super(message);
    }

    @Override
    public String getErrorCode() {
        return errorCode;
    }
}
