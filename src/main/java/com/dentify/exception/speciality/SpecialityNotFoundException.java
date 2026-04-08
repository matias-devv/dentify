package com.dentify.exception.speciality;

import com.dentify.exception.dto.AppException;

public class SpecialityNotFoundException extends RuntimeException implements AppException {

    private final String errorCode = "SPECIALITY_NOT_FOUND";

    public SpecialityNotFoundException(String message) {
        super(message);
    }

    @Override
    public String getErrorCode() {
        return errorCode;
    }
}