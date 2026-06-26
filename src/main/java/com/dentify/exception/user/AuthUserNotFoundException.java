package com.dentify.exception.user;

import com.dentify.exception.dto.AppException;

public class AuthUserNotFoundException extends RuntimeException implements AppException {
    private final String errorCode = "AUTH_USER_NOT_FOUND";

    public AuthUserNotFoundException(String message) {
        super(message);
    }

    @Override
    public String getErrorCode() { return errorCode; }
}