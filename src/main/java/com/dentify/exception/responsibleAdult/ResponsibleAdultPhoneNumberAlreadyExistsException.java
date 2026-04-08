package com.dentify.exception.responsibleAdult;

import com.dentify.exception.dto.AppException;

public class ResponsibleAdultPhoneNumberAlreadyExistsException extends RuntimeException implements AppException {

    private final String errorCode = "RESPONSIBLE_ADULT_PHONE_ALREADY_EXISTS";

    public ResponsibleAdultPhoneNumberAlreadyExistsException(String phoneNumber) {
        super("A responsible adult with phone number " + phoneNumber + " already exists");
    }

    @Override
    public String getErrorCode() {
        return errorCode;
    }
}