package com.dentify.exception.general;

import com.dentify.exception.dto.AppException;

public class FileStorageException extends RuntimeException implements AppException {

    private final String errorCode = "FILE_STORAGE_EXCEPTION";

    public FileStorageException(String message) {
        super(message);
    }

    @Override
    public String getErrorCode() {
        return errorCode;
    }
}
