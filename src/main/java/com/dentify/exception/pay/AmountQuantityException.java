package com.dentify.exception.pay;

import com.dentify.exception.dto.AppException;

public class AmountQuantityException extends RuntimeException implements AppException {

    private final String errorCode = "AMOUNT_QUANTITY_INVALID";

    public AmountQuantityException(String message) {
        super(message);
    }

    @Override
    public String getErrorCode() {
        return errorCode;
    }
}
