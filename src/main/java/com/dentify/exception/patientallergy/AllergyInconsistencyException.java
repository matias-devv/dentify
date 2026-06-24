package com.dentify.exception.patientallergy;

import com.dentify.exception.dto.AppException;

public class AllergyInconsistencyException extends RuntimeException implements AppException {

    private final String errorCode = "PATIENT_ALLERGY_INCONSISTENCY";

    public AllergyInconsistencyException(String message) {
        super(message);
    }

    @Override
    public String getErrorCode() {
        return errorCode;
    }
}
