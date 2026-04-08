package com.dentify.exception.agenda;

import com.dentify.exception.dto.AppException;

public class AgendaOwnershipException extends RuntimeException  implements AppException {
    private final String errorCode = "AGENDA_OWNERSHIP_VIOLATION";
    public AgendaOwnershipException(String message) { super(message); }
    public String getErrorCode() { return errorCode; }
}