package com.dentify.exception.speciality;

import com.dentify.exception.dto.AppException;

public class SpecialitiesRequiredException extends RuntimeException implements AppException {

    private final String errorCode = "SPECIALITIES_REQUIRED";

    public SpecialitiesRequiredException(String message) {
        super(message);
    }

    public String getErrorCode() {
        return errorCode;
    }
}