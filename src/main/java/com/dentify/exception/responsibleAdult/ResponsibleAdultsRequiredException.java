package com.dentify.exception.responsibleAdult;

import com.dentify.exception.dto.AppException;

public class ResponsibleAdultsRequiredException extends RuntimeException implements AppException {

    private final String errorCode = "RESPONSIBLE_ADULTS_REQUIRED";

    public ResponsibleAdultsRequiredException(String message) {
        super(message);
    }

    @Override
    public String getErrorCode() {
        return errorCode;
    }
}