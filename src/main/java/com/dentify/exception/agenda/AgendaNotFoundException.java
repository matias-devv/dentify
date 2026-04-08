package com.dentify.exception.agenda;

import com.dentify.exception.dto.AppException;

public class AgendaNotFoundException extends RuntimeException  implements AppException {
    private final String errorCode = "AGENDA_NOT_FOUND";
    public AgendaNotFoundException(String message) { super(message); }
    public String getErrorCode() { return errorCode; }
}