package com.dentify.domain.exception.invitation;

import com.dentify.domain.exception.dto.AppException;

public class EmailMismatchException extends RuntimeException implements AppException {
    private final String errorCode = "EMAIL_MISMATCH";
    public EmailMismatchException(String message) { super(message); }
    public String getErrorCode() { return errorCode; }
}