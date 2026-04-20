package com.dentify.domain.invitation.dto.response;

import com.dentify.domain.invitation.enums.InvitationStatus;
import com.dentify.domain.invitation.enums.InvitedRole;

public record CreateInvitationResponse(Long id,
                                       String email,
                                       InvitedRole invitedRole,
                                       String invitedBy,         // name of the inviter
                                       InvitationStatus status,
                                       String expiresAt) {
}
