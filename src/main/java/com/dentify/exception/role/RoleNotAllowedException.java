package com.dentify.exception.role;

import com.dentify.exception.dto.AppException;

public class RoleNotAllowedException extends RuntimeException implements AppException {

    private static final String ERROR_CODE = "ROLE_NOT_ALLOWED";

    public RoleNotAllowedException(String message) {
        super(message);
    }

    public String getErrorCode() {
        return ERROR_CODE;
    }
}