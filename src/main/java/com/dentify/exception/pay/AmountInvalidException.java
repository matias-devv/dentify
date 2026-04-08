package com.dentify.exception.pay;

import com.dentify.exception.dto.AppException;

public class AmountInvalidException extends RuntimeException implements AppException {

    private final String errorCode = "AMOUNT_INVALID";

    public AmountInvalidException(String message) {
        super(message);
    }

    @Override
    public String getErrorCode() {
        return errorCode;
    }
}