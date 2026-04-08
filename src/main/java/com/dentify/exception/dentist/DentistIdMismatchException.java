package com.dentify.exception.dentist;

import com.dentify.exception.dto.AppException;

public class DentistIdMismatchException extends RuntimeException implements AppException {

    String errorCode = "DENTIST_ID_MISMATCH";

    public DentistIdMismatchException(String message) {
        super(message);
    }

    @Override
    public String getErrorCode() {
        return errorCode;
    }
}