package com.dentify.exception.agenda;

import com.dentify.exception.dto.AppException;

public class DayOfAgendaNotFoundException extends RuntimeException implements AppException {

    private final String errorCode = "DAY_OF_AGENDA_NOT_FOUND";

    public DayOfAgendaNotFoundException(String message) {
        super(message);
    }

    @Override
    public String getErrorCode() {
        return errorCode;
    }
}