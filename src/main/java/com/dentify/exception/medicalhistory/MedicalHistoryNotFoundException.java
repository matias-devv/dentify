package com.dentify.exception.medicalhistory;

import com.dentify.exception.dto.AppException;

public class MedicalHistoryNotFoundException extends RuntimeException implements AppException {

    private static final String errorCode = "MEDICAL_HISTORY_NOT_FOUND";

    public MedicalHistoryNotFoundException(String message) {
        super(message);
    }

    @Override
    public String getErrorCode() {
        return errorCode;
    }
}
