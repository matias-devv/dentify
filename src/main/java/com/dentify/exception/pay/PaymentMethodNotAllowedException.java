package com.dentify.exception.pay;

import com.dentify.exception.dto.AppException;

public class PaymentMethodNotAllowedException extends RuntimeException implements AppException {

    private final String errorCode = "PAYMENT_METHOD_NOT_ALLOWED";

    public PaymentMethodNotAllowedException(String message) {
        super(message);
    }

    @Override
    public String getErrorCode() {
        return errorCode;
    }
}