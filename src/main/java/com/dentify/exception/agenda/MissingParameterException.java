package com.dentify.exception.agenda;

import com.dentify.exception.dto.AppException;

public class MissingParameterException extends RuntimeException implements AppException {
    private final String errorCode = "MISSING_PARAMETER";
    public MissingParameterException(String message) { super(message); }
    public String getErrorCode() { return errorCode; }
}