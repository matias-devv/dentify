package com.dentify.domain.exception.user;

import com.dentify.domain.exception.dto.AppException;

public class AuthUserNotFoundException extends RuntimeException implements AppException {
    private final String errorCode = "AUTH_USER_NOT_FOUND";

    public AuthUserNotFoundException(String message) {
        super(message);
    }

    public String getErrorCode() { return errorCode; }
}