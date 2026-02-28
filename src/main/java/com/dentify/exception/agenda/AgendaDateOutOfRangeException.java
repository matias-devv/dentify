package com.dentify.domain.exception.agenda;

public class AgendaDateOutOfRangeException extends RuntimeException {
    private final String errorCode = "AGENDA_DATE_OUT_OF_RANGE";
    public AgendaDateOutOfRangeException(String message) { super(message); }
    public String getErrorCode() { return errorCode; }
}
