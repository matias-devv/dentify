package com.dentify.domain.exception.pay;

import com.dentify.domain.exception.dto.AppException;

public class PaymentRequiredException extends RuntimeException implements AppException {
    private final String errorCode = "PAYMENT_REQUIRED";
    public PaymentRequiredException(String message) { super(message); }
    public String getErrorCode() { return errorCode; }
}