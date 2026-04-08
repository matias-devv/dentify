package com.dentify.exception.pay;

import com.dentify.exception.dto.AppException;

public class PaymentNotFoundException extends RuntimeException implements AppException {

    private final String errorCode = "PAYMENT_NOT_FOUND";

    public PaymentNotFoundException(String message) {
        super(message);
    }

    @Override
    public String getErrorCode() {
        return errorCode;
    }
}
