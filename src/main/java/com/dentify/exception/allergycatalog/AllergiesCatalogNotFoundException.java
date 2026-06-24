package com.dentify.exception.allergycatalog;

import com.dentify.exception.dto.AppException;

public class AllergiesCatalogNotFoundException extends RuntimeException implements AppException {

    private final String errorCode = "ALLERGIES_CATALOG_NOT_FOUND";

    public AllergiesCatalogNotFoundException(String message) {
        super(message);
    }

    @Override
    public String getErrorCode() {
        return errorCode;
    }
}
