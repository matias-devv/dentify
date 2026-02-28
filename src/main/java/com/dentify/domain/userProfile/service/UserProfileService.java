package com.dentify.domain.userProfile.service;

import com.dentify.common.util.JwtUtils;
import com.dentify.domain.speciality.model.Speciality;
import com.dentify.domain.speciality.service.ISpecialityService;
import com.dentify.domain.userProfile.dto.request.UpdateUserProfileRequest;
import com.dentify.domain.userProfile.dto.response.UserProfileResponse;
import com.dentify.domain.userProfile.dto.response.UserSummaryResponse;
import com.dentify.mapper.UserAppMapper;
import com.dentify.domain.userProfile.model.UserProfile;
import com.dentify.domain.userProfile.repository.IAppUserRepository;
import com.dentify.security.dto.request.RegisterUserRequest;
import com.dentify.mapper.AuthUserMapper;
import com.dentify.security.model.AuthUser;
import com.dentify.security.model.Role;
import com.dentify.security.repository.IAuthUserRepository;
import com.dentify.security.service.IRoleService;
import com.dentify.security.service.UserDetailsServiceImp;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService implements IUserService {

    private final IAppUserRepository appUserRepository;
    private final IAuthUserRepository authUserRepository;
    private final ISpecialityService specialityService;
    private final IRoleService roleService;
    private final JwtUtils jwtUtils;
    private final UserDetailsServiceImp userDetailsService;
    private final UserAppMapper userAppMapper;
    private final AuthUserMapper authUserMapper;

    //I assume that during login it validated whether the user existed before this method
    @Override
    public boolean saveUser(RegisterUserRequest request) {

        UserProfile userProfile = userAppMapper.setAttributesToAppUser( request);
        AuthUser authUser = authUserMapper.setAttributesToAuthUser(request);

        if( request.id_speciality() != null) {
            userProfile = this.setSpecialityToAppUser(userProfile, request.id_speciality());
        }

        authUser = this.setRolesToAuthUser( authUser, request.id_role() );
        
        appUserRepository.save(userProfile);

        authUserRepository.save( authUser );

        this.setRelationBetweenAppUserAndAuthUser(userProfile, authUser);

        return true;
    }

    private void setRelationBetweenAppUserAndAuthUser(UserProfile userProfile, AuthUser authUser) {

        authUser.setAppUser(userProfile);
        userProfile.setAuth_user(authUser);

        authUserRepository.save( authUser );
        appUserRepository.save(userProfile);
    }

    private AuthUser setRolesToAuthUser(AuthUser authUser, Long idRole) {

        Optional<Role> role = roleService.getRole(idRole);

        if( role.isPresent() ) {

            Set<Role> roles = authUser.getRoles();

            if( !roles.contains( role.get() ) ) {

                roles.add( role.get() );

                authUser.setRoles( roles );
            }
        }
        return authUser;
    }

    private UserProfile setSpecialityToAppUser(UserProfile userProfile, Long idSpeciality) {

        Speciality speciality = specialityService.getSpecialityEntityById(idSpeciality);

        if( speciality != null) {

            Set<Speciality> specialities = userProfile.getSpecialities();

            if( !specialities.contains( speciality ) ) {

                specialities.add( speciality );

                userProfile.setSpecialities( specialities );
            }
        }
        return userProfile;
    }


    @Override
    public UserProfile findUserAppEntityById(Long id) {
        return appUserRepository.findById( id ).orElseThrow( () -> new RuntimeException("App User not found"));
    }

    @Override
    public UserProfile validateIfUserExists(Long id_user_app) {

        UserProfile userProfile = this.findUserAppEntityById(id_user_app);

        if(userProfile == null) {
            return null;
        }
        return userProfile;
    }

    @Override
    public UserProfileResponse findMe( UserDetails userDetails) {

        UserProfile userProfile = this.findUserAppEntityByUsername( userDetails.getUsername() );

        return userAppMapper.buildUserProfileResponse( userDetails, userProfile);
    }

    public UserProfile findUserAppEntityByUsername(String username) {

        Optional<UserProfile> appUser = appUserRepository.findByAuthUsername(username);

        if ( appUser.isPresent() ) {
            return appUser.get();
        }
        else{
            throw new UsernameNotFoundException("There is no user associated with this username");
        }
    }

    @Override
    public UserProfileResponse updateUserApp(UserDetails userDetails, UpdateUserProfileRequest request) {

        UserProfile userProfile = this.findUserAppEntityByUsername(userDetails.getUsername());

        userProfile = userAppMapper.setAttributesToUpdateAppUser(request, userProfile);

        appUserRepository.save(userProfile);

        return userAppMapper.buildUserProfileResponse(userDetails, userProfile);
    }

    @Override
    public List<UserSummaryResponse> getAllAppUserSummaryFromDentists() {

        List<UserProfile> users = appUserRepository.findAllByRoleName("ROLE_DENTIST");
        List<UserSummaryResponse> responseList = new ArrayList<>();

        for (UserProfile userProfile : users) {

            responseList.add( userAppMapper.buildUserSummaryResponse(userProfile) );
        }
        return responseList;
    }

    @Override
    public String deactiveAppUser(Long id) {

        UserProfile userProfile = appUserRepository.findByIdWithAuthUser( id ).orElseThrow( () -> new RuntimeException("App User not found"));

        userProfile.getAuth_user().setEnabled(false);
        userProfile.getAuth_user().setAccountNotLocked(false);

        return "The user was successfully deactivated";
    }

    @Override
    public UserProfile findByIdWithAuthUser(Long id) {
        return appUserRepository.findByIdWithAuthUser( id ).orElseThrow( () -> new RuntimeException("App User not found"));
    }

}
