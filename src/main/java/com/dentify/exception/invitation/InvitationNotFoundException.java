package com.dentify.exception.invitation;

import com.dentify.exception.dto.AppException;

public class InvitationNotFoundException extends RuntimeException implements AppException {

    private final String errorCode = "INVITATION_NOT_FOUND";

    public InvitationNotFoundException(String message) { super(message); }

    public String getErrorCode() { return errorCode; }
}