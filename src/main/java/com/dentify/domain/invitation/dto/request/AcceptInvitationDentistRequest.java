package com.dentify.domain.invitation.dto.request;

import com.dentify.domain.clinic.dto.ClinicData;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AcceptInvitationDentistRequest(@NotBlank String token,

                                             //Personal data
                                             @NotBlank String name,
                                             @NotBlank String surname,
                                             @NotBlank @Email String email,
                                             @NotBlank String phone,
                                             @NotBlank String dni,
                                             @NotBlank @Size(min = 8) String password,

                                             ClinicData clinic ) // <- null if this invitation is for the secretary
{ }
