package com.dentify.domain.exception.invitation;

import com.dentify.domain.exception.dto.AppException;

public class InvalidInvitationStatusException extends RuntimeException implements AppException {
    private final String errorCode = "INVALID_INVITATION_STATUS";

    public InvalidInvitationStatusException(String message) {
        super(message);
    }

    public String getErrorCode() { return errorCode; }
}