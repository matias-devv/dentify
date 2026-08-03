package com.dentify.exception.general;

import com.dentify.exception.dto.AppException;

public class FileTooLargeException extends RuntimeException implements AppException {

    private final String errorCode = "FILE_TOO_LARGE_EXCEPTION";

    public FileTooLargeException(String message) {
        super(message);
    }

    @Override
    public String getErrorCode() {
        return errorCode;
    }
}
