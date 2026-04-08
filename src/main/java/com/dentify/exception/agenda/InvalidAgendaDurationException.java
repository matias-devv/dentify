package com.dentify.exception.agenda;

import com.dentify.exception.dto.AppException;

public class InvalidAgendaDurationException extends RuntimeException implements AppException {

    private final String errorCode = "INVALID_AGENDA_DURATION";

    public InvalidAgendaDurationException(String message) {
        super(message);
    }

    @Override
    public String getErrorCode() {
        return errorCode;
    }
}
