package com.dentify.exception.agenda;

import com.dentify.exception.dto.AppException;

public class AgendaMonthOutOfRangeException extends RuntimeException implements AppException {

    private final String errorCode = "AGENDA_MONTH_OUT_OF_RANGE";

    public AgendaMonthOutOfRangeException(String message) {
        super(message);
    }

    @Override
    public String getErrorCode() {
        return errorCode;
    }
}
