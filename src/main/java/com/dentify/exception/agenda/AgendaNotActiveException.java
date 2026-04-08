package com.dentify.exception.agenda;

import com.dentify.exception.dto.AppException;

public class AgendaNotActiveException extends RuntimeException implements AppException {
    private final String errorCode = "AGENDA_NOT_ACTIVE";
    public AgendaNotActiveException(String message) { super(message); }
    public String getErrorCode() { return errorCode; }
}
