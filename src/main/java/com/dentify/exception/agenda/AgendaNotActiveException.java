package com.dentify.domain.exception.agenda;

public class AgendaNotActiveException extends RuntimeException {
    private final String errorCode = "AGENDA_NOT_ACTIVE";
    public AgendaNotActiveException(String message) { super(message); }
    public String getErrorCode() { return errorCode; }
}
