package com.dentify.domain.exception.agenda;

public class InvalidAgendaDateException extends RuntimeException {
    private final String errorCode = "INVALID_AGENDA_DATE";
    public InvalidAgendaDateException(String message) { super(message); }
    public String getErrorCode() { return errorCode; }
}