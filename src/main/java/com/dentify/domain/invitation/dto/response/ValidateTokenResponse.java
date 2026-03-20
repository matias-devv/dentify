package com.dentify.domain.invitation.dto.response;

public record ValidateTokenResponse(String dentistName,
                                    String email,
                                    String invitedByRole) {
}
