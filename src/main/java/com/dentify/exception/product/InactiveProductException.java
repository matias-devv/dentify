package com.dentify.exception.product;

import com.dentify.exception.dto.AppException;

public class InactiveProductException extends RuntimeException implements AppException {


    private final String errorCode = "PRODUCT_NOT_ACTIVE";

    public InactiveProductException(String message) {
        super(message);
    }

    @Override
    public String getErrorCode() {
        return errorCode;
    }
}
