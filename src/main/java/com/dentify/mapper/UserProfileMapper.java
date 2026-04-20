package com.dentify.mapper;

import com.dentify.domain.userProfile.dto.request.UpdateUserProfileRequest;
import com.dentify.domain.userProfile.dto.response.UserProfileResponse;
import com.dentify.domain.userProfile.dto.response.UserSummaryResponse;
import com.dentify.domain.userProfile.model.UserProfile;
import com.dentify.security.model.AuthUser;
import com.dentify.security.model.Role;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class UserProfileMapper {

    public UserProfileResponse buildUserProfileResponse(AuthUser authUser, UserProfile userProfile){

        List<String> rolesList = new ArrayList<>();

        for (Role role : authUser.getRoles()) {
                rolesList.add( role.getRoleName() );
        }

        return new UserProfileResponse( userProfile.getId(),
                                        userProfile.getName(),
                                        userProfile.getSurname(),
                                        userProfile.getClinic().getName(),
                                        userProfile.getClinic().getId(),
                                        userProfile.getDni(),
                                        userProfile.getPhone_number(),
                                        rolesList);
    }

    public UserProfile setAttributesToUpdateUserProfile(UpdateUserProfileRequest request, UserProfile userProfile) {
        userProfile.setName(request.name());
        userProfile.setSurname(request.surname());
        userProfile.setPhone_number(request.phone_number());
        userProfile.setUpdatedAt(LocalDateTime.now());
        return userProfile;
    }

    public UserSummaryResponse buildUserSummaryResponse(UserProfile userProfile) {
        return new UserSummaryResponse(userProfile.getId(),
                                       userProfile.getName(),
                                       userProfile.getSurname(),
                                       userProfile.getDentist().getSpecialities().stream().findFirst().get().getName() );
    }

    public UserProfile buildUserProfile(String name, String surname, String phone, String dni) {
        return UserProfile.builder()
                .name(name)
                .surname(surname)
                .phone_number( phone)
                .dni( dni)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public UserProfile buildPlatformUserProfile() {
        return UserProfile.builder()
                .name("Matias")
                .surname("Rodriguez")
                .phone_number( "1122334455")
                .dni("11.111.111")
                .createdAt(LocalDateTime.now())
                .build();
    }
}
