package com.dentify.exception.invitation;

import com.dentify.exception.dto.AppException;

public class EmailMismatchException extends RuntimeException implements AppException {
    private final String errorCode = "EMAIL_MISMATCH";
    public EmailMismatchException(String message) { super(message); }
    public String getErrorCode() { return errorCode; }
}