package com.dentify.domain.exception.agenda;

public class InvalidAgendaNameException extends RuntimeException {
    private final String errorCode = "INVALID_AGENDA_NAME";
    public InvalidAgendaNameException(String message) { super(message); }
    public String getErrorCode() { return errorCode; }
}