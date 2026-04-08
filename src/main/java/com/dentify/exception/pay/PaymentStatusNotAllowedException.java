package com.dentify.exception.pay;

import com.dentify.exception.dto.AppException;

public class PaymentStatusNotAllowedException extends RuntimeException implements AppException {

    private final String errorCode = "PAYMENT_STATUS_NOT_ALLOWED";

    public PaymentStatusNotAllowedException(String message) {
        super(message);
    }

    @Override
    public String getErrorCode() {
        return errorCode;
    }
}