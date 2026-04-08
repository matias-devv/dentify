package com.dentify.domain.invitation.dto.request;

import com.dentify.domain.clinic.dto.ClinicData;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record AcceptInvitationRequest(@NotBlank String token,

                                      @NotBlank String name,

                                      @NotBlank String surname,

                                      @NotBlank String dni,

                                      @NotBlank @Email String email,

                                      @NotBlank String phone,

                                      @NotBlank @Size(min = 8) String password,

                                      //nullable if the invitation request is for secretary user
                                      ClinicData clinic,
                                      //nullable if the invitation request is for secretary user
                                      Set<Long> specialities) {

}