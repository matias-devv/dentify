package com.dentify.exception.auth;

import com.dentify.exception.dto.AppException;

public class RefreshTokenException extends RuntimeException implements AppException {

    private final String errorCode = "REFRESH_TOKEN_INVALID";

    public RefreshTokenException(String message) {
        super(message);
    }

    @Override
    public String getErrorCode() {
        return errorCode;
    }
}