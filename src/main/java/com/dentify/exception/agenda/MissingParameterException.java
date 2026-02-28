package com.dentify.domain.exception.agenda;

public class MissingParameterException extends RuntimeException {
    private final String errorCode = "MISSING_PARAMETER";
    public MissingParameterException(String message) { super(message); }
    public String getErrorCode() { return errorCode; }
}