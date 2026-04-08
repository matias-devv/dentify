package com.dentify.exception.invitation;

import com.dentify.exception.dto.AppException;

public class PendingInvitationException extends RuntimeException implements AppException {

    private final String errorCode = "PENDING_INVITATION_EXISTS";

    public PendingInvitationException(String message) {
        super(message);
    }

    public String getErrorCode() {
        return errorCode;
    }
}