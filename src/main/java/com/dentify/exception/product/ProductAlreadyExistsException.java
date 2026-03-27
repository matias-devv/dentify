package com.dentify.exception.product;

import com.dentify.exception.dto.AppException;

public class ProductAlreadyExistsException extends RuntimeException implements AppException {

    private final String errorCode = "PRODUCT_ALREADY_EXISTS";

    public ProductAlreadyExistsException(String message) {
        super(message);
    }

    @Override
    public String getErrorCode() {
        return errorCode;
    }
}
