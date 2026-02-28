package com.dentify.domain.exception.invitation;

import com.dentify.domain.exception.dto.AppException;

public class InvitationExpiredException extends RuntimeException implements AppException {
    private final String errorCode = "INVITATION_EXPIRED";

    public InvitationExpiredException(String message) {
        super(message);
    }

    public String getErrorCode() {
        return errorCode;
    }
}