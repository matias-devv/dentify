package com.dentify.domain.invitation.dto.response;

public record AcceptInvitationResponse(String message,
                                       String email,
                                       String role) {
}