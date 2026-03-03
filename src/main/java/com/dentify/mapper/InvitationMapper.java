package com.dentify.mapper;

import com.dentify.domain.clinic.model.Clinic;
import com.dentify.domain.invitation.dto.request.AcceptInvitationRequest;
import com.dentify.domain.invitation.dto.request.CreateInvitationRequest;
import com.dentify.domain.invitation.dto.response.AcceptInvitationResponse;
import com.dentify.domain.invitation.dto.response.CreateInvitationResponse;
import com.dentify.domain.invitation.enums.InvitationStatus;
import com.dentify.domain.invitation.model.Invitation;
import com.dentify.domain.userProfile.model.UserProfile;
import com.dentify.integration.email.service.GenerateMailTokenService;
import com.dentify.security.model.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
public class InvitationMapper {

    private final GenerateMailTokenService mailTokenService;

    public Invitation buildInvitation( CreateInvitationRequest request, AuthUser authInviter, Clinic clinic, Long dentistId) {
        return Invitation.builder()
                .email( request.email() )
                .token( mailTokenService.generateConfirmationToken() )
                .invitedRole( request.invitedRole() )
                .invitedBy( authInviter )
                .clinic( clinic )                                 //It can be null if the inviter is admin.
                .dentistId( dentistId )                          //It can be null if the inviter is admin.
                .status( InvitationStatus.PENDING )
                .expiresAt( LocalDateTime.now().plusHours(48) )
                .build();
    }

    public CreateInvitationResponse buildCreateInvitationResponse(CreateInvitationRequest request, UserProfile inviterProfile, Invitation invitation) {
        return new CreateInvitationResponse(invitation.getId(),
                request.email(),
                request.invitedRole(),
                inviterProfile.getName() + " " + inviterProfile.getSurname(),
                InvitationStatus.PENDING,
                invitation.getExpiresAt().format( DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm" ) ) );
    }

    public AcceptInvitationResponse buildAcceptInvitationResponse( String message, String email, String invitedRole){
        return new AcceptInvitationResponse(message, email, invitedRole);
    }
}
