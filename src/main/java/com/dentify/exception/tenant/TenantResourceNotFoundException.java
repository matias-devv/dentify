package com.dentify.exception.tenant;

import com.dentify.exception.dto.AppException;

public class TenantResourceNotFoundException extends RuntimeException implements AppException {

    private final String errorCode = "TENANT_RESOURCE_NOT_FOUND";

    public TenantResourceNotFoundException(String message) {
        super(message);
    }

    @Override
    public String getErrorCode() {
        return errorCode;
    }
}