package com.dentify.domain.exception.agenda;

public class AgendaOwnershipException extends RuntimeException {
    private final String errorCode = "AGENDA_OWNERSHIP_VIOLATION";
    public AgendaOwnershipException(String message) { super(message); }
    public String getErrorCode() { return errorCode; }
}