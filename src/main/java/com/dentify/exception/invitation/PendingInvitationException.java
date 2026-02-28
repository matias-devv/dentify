package com.dentify.domain.exception.invitation;

import com.dentify.domain.exception.dto.AppException;

public class PendingInvitationException extends RuntimeException implements AppException {

    private final String errorCode = "PENDING_INVITATION_EXISTS";

    public PendingInvitationException(String message) {
        super(message);
    }

    public String getErrorCode() {
        return errorCode;
    }
}