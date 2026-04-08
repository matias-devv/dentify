package com.dentify.exception.agenda;

import com.dentify.exception.dto.AppException;

public class InvalidAgendaDateException extends RuntimeException implements AppException {

    private final String errorCode = "INVALID_AGENDA_DATE";

    public InvalidAgendaDateException(String message) { super(message); }

    public String getErrorCode() { return errorCode; }
}