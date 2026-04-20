package com.dentify.domain.userProfile.service;

import com.dentify.domain.clinic.model.Clinic;
import com.dentify.domain.invitation.dto.request.AcceptInvitationRequest;
import com.dentify.domain.userProfile.dto.request.UpdateUserProfileRequest;
import com.dentify.domain.userProfile.dto.response.UserProfileResponse;
import com.dentify.domain.userProfile.dto.response.UserSummaryResponse;
import com.dentify.exception.clinic.ClinicNotFoundException;
import com.dentify.mapper.UserProfileMapper;
import com.dentify.domain.userProfile.model.UserProfile;
import com.dentify.domain.userProfile.repository.IUserProfileRepository;
import com.dentify.security.model.AuthUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserProfileService implements IUserProfileService {

    private final IUserProfileRepository userProfileRepository;
    private final UserProfileMapper userProfileMapper;

    @Override
    public UserProfileResponse findMe( String username) {

        UserProfile userProfile = this.findByAuthUsername( username );

        return userProfileMapper.buildUserProfileResponse( userProfile.getAuthUser(), userProfile);
    }

    public UserProfile findByAuthUsername(String username) {

        Optional<UserProfile> appUser = userProfileRepository.findByAuthUsername( username);

        if ( appUser.isPresent() ) {
            return appUser.get();
        }
        else{
            throw new UsernameNotFoundException( "There is no auth user associated with this username");
        }
    }

    @Override
    public UserProfile findByAuthUsernameWithoutClinic(String username) {

        Optional<UserProfile> appUser = userProfileRepository.findByAuthUsernameWithoutClinic( username);

        if ( appUser.isPresent() ) {
            return appUser.get();
        }
        else{
            throw new UsernameNotFoundException( "There is no auth user associated with this username");
        }
    }

    @Override
    public Clinic findClinicByAuthUserUsername(String username) {
        return userProfileRepository.findClinicByAuthUserUsername(username)
                                    .orElseThrow( ()-> new ClinicNotFoundException("This user profile has no clinic associated"));
    }

    @Override
    public UserProfile createUserProfileByInvitation( AcceptInvitationRequest request, Clinic clinic, AuthUser newAuthUser ) {

        UserProfile newUserProfile = userProfileMapper.buildUserProfile( request.name(), request.surname(), request.phone(), request.dni() );

        newUserProfile.setClinic( clinic);

        newUserProfile.setAuthUser( newAuthUser);

        this.persistUserProfile( newUserProfile);

        return newUserProfile;
    }

    @Override
    public void persistUserProfile(UserProfile newUserProfile) {
        userProfileRepository.save( newUserProfile);
    }

    @Override
    public UserProfile findByAuthUserUsernameWithClinic(String username) {
        return userProfileRepository.findByAuthUserUsernameWithClinic( username)
                                    .orElseThrow( ()-> new UsernameNotFoundException("The user with this email was not found"));
    }

    @Override
    public UserProfileResponse updateUserProfile(String username, UpdateUserProfileRequest request) {

        UserProfile userProfile = this.findByAuthUsername( username);

        userProfile = userProfileMapper.setAttributesToUpdateUserProfile( request, userProfile);

        userProfileRepository.save( userProfile);

        return userProfileMapper.buildUserProfileResponse( userProfile.getAuthUser(), userProfile);
    }

    @Override
    public List<UserSummaryResponse> getAllUserProfileSummaryFromDentists() {

        List<UserProfile> users = userProfileRepository.findAllByRoleName("ROLE_DENTIST");

        List<UserSummaryResponse> responseList = new ArrayList<>();

        for (UserProfile userProfile : users) {

            responseList.add( userProfileMapper.buildUserSummaryResponse(userProfile) );
        }
        return responseList;
    }

    @Override
    public String deactiveUserProfile(Long id) {

        UserProfile userProfile = userProfileRepository.findByIdWithAuthUser( id ).orElseThrow( () -> new RuntimeException("App User not found"));

        userProfile.setUpdatedAt( LocalDateTime.now());

        userProfile.getAuthUser().setEnabled( false);

        userProfile.getAuthUser().setUpdatedAt( LocalDateTime.now());

        userProfile.getAuthUser().setAccountNonLocked( false);

        userProfileRepository.save( userProfile);

        return "The user was successfully deactivated";
    }

    @Override
    public void createPlatformUserProfile(AuthUser newAdmin) {

        UserProfile newUserProfile = userProfileMapper.buildPlatformUserProfile();

        newUserProfile.setAuthUser(newAdmin);

        userProfileRepository.save(newUserProfile);
    }

}
