package com.dentify.exception.product;

import com.dentify.exception.dto.AppException;

public class ProductNotFoundException extends RuntimeException implements AppException {
    private final String errorCode = "PRODUCT_NOT_FOUND";
    public ProductNotFoundException(String message) { super(message); }
    public String getErrorCode() { return errorCode; }
}
