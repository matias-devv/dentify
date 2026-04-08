package com.dentify.exception.responsibleAdult;

import com.dentify.exception.dto.AppException;

public class ResponsibleAdultEmailAlreadyExistsException extends RuntimeException implements AppException {

    private final String errorCode = "RESPONSIBLE_ADULT_EMAIL_ALREADY_EXISTS";

    public ResponsibleAdultEmailAlreadyExistsException(String email) {
        super("A responsible adult with email " + email + " already exists");
    }

    @Override
    public String getErrorCode() {
        return errorCode;
    }
}