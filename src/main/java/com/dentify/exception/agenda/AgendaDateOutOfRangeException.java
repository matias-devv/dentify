package com.dentify.exception.agenda;

import com.dentify.exception.dto.AppException;

public class AgendaDateOutOfRangeException extends RuntimeException implements AppException {
    private final String errorCode = "AGENDA_DATE_OUT_OF_RANGE";

    public AgendaDateOutOfRangeException(String message) { super(message); }

    public String getErrorCode() { return errorCode; }
}
