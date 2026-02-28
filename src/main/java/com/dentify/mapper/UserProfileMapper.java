package com.dentify.mapper;

import com.dentify.domain.userProfile.dto.request.UpdateUserProfileRequest;
import com.dentify.domain.userProfile.dto.response.UserProfileResponse;
import com.dentify.domain.userProfile.dto.response.UserSummaryResponse;
import com.dentify.domain.userProfile.model.UserProfile;
import com.dentify.security.dto.request.RegisterUserRequest;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class UserAppMapper {

    public UserProfileResponse buildUserProfileResponse(UserDetails userDetails, UserProfile userProfile){

        List<String> rolesList = new ArrayList<>();

        for (GrantedAuthority grantedAuthority : userDetails.getAuthorities()) {

            if ( grantedAuthority.getAuthority().startsWith("ROLE_")) {

                rolesList.add( grantedAuthority.getAuthority());
            }
        }

        return new UserProfileResponse( userProfile.getId_app_user(),
                                        userProfile.getName(),
                                        userProfile.getSurname(),
                                        userProfile.getClinic_name(),
                                        userProfile.getDni(),
                                        userProfile.getPhone_number(),
                                        rolesList);
    }

    public UserProfile setAttributesToAppUser(RegisterUserRequest request) {
        UserProfile userProfile = new UserProfile();
        userProfile.setName(request.name());
        userProfile.setSurname(request.surname());
        userProfile.setDni(request.dni());
        userProfile.setPhone_number(request.phone_number());
        return userProfile;
    }

    public UserProfile setAttributesToUpdateAppUser(UpdateUserProfileRequest request, UserProfile userProfile) {
        userProfile.setName(request.name());
        userProfile.setSurname(request.surname());
        userProfile.setPhone_number(request.phone_number());
        userProfile.setClinic_name(request.clinic_name());
        return userProfile;
    }

    public UserSummaryResponse buildUserSummaryResponse(UserProfile userProfile) {
        return new UserSummaryResponse(userProfile.getId_app_user(),
                                       userProfile.getName(),
                                       userProfile.getSurname(),
                                       userProfile.getSpecialities().stream().findFirst().get().getName() );
    }
}
