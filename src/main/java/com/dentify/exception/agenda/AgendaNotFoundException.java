package com.dentify.domain.exception.agenda;

public class AgendaNotFoundException extends RuntimeException {
    private final String errorCode = "AGENDA_NOT_FOUND";
    public AgendaNotFoundException(String message) { super(message); }
    public String getErrorCode() { return errorCode; }
}