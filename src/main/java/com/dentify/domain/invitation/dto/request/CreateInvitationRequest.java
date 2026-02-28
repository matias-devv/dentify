package com.dentify.domain.invitation.dto.request;

import com.dentify.domain.invitation.enums.InvitedRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SendInvitationRequestTo(@NotBlank @Email
                                      String email,

                                      @NotNull
                                      InvitedRole invitedRole)   // enum: DENTIST, SECRETARY)
{
}
