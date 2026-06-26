package com.dentify.security.service;

import com.dentify.common.util.JwtUtils;
import com.dentify.domain.userProfile.model.UserProfile;
import com.dentify.domain.userProfile.service.IUserProfileService;
import com.dentify.exception.user.AuthUserNotFoundException;
import com.dentify.exception.user.UserAlreadyExistsException;
import com.dentify.mapper.AuthUserMapper;
import com.dentify.security.dto.request.AuthLoginRequest;
import com.dentify.security.dto.response.AuthResponse;
import com.dentify.security.dto.response.LoginResult;
import com.dentify.security.model.AuthUser;
import com.dentify.security.model.Role;
import com.dentify.security.repository.IAuthUserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 *
 *  1. {@link RefreshTokenService} injected via constructor.
 *  2. {@code loginUser} now creates a refresh token and returns it
 *     alongside the access token.
 *  3. Access-token lifetime is still controlled by {@link JwtUtils}
 *
 * Everything else (tenantId resolution, multi-tenant lookup, password
 * validation) is unchanged.
 */
@Service
@RequiredArgsConstructor
public class AuthUserService implements IAuthUserService {

    //repository
    private final IAuthUserRepository authUserRepository;

    //utils
    private final JwtUtils jwtUtils;
    private final PasswordEncoder  passwordEncoder;

    //services
    private final UserDetailsServiceImp userDetailsService;
    private final IRoleService roleService;
    private final IUserProfileService userProfileService;

    //mapper
    private final AuthUserMapper authUserMapper;

    public static final String PLATFORM_TENANT = "00000000-0000-0000-0000-000000000000";

    /**
     * Authenticates the user and returns both an access token and a raw
     * refresh-token value.
     *
     * The controller is responsible for putting the refresh token into an
     * httpOnly cookie; this service is cookie-agnostic.
     *
     * @return {@link LoginResult} — a lightweight value object that bundles
     *         the {@link AuthResponse} (for the body) and the raw refresh
     *         token string (for the cookie).
     */
    @Override
    public LoginResult loginUser(AuthLoginRequest userRequest) {

        Authentication authentication = this.authenticate( userRequest.email(), userRequest.password());

        SecurityContextHolder.getContext().setAuthentication(authentication);

        AuthUser authUser = this.findByUsernameWithProfileAndClinic( userRequest.email());

        String tenantId = this.resolveTenant(authUser);

        String accessToken = jwtUtils.createToken(authentication, tenantId);

        AuthResponse body = new AuthResponse( userRequest.email(), "login successful", accessToken, tenantId, true);

        // Return the AuthUser so the controller can create the refresh token
        return new LoginResult(body, authUser);
    }

    private String resolveTenant(AuthUser authUser) {

        if ( authUser.hasRole("ADMIN") ) {
            return PLATFORM_TENANT;
        }

        return resolveClinicTenant(authUser);
    }

    public String resolveClinicTenant(AuthUser authUser) {

        UserProfile profile = authUser.getUserProfile();

        if (profile == null || profile.getClinic() == null) {
            throw new IllegalStateException("User without clinic cannot resolve tenant");
        }

        return profile.getClinic().getTenantId();
    }

    public Authentication authenticate(String email, String password) {

        //find user
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);

        if( userDetails == null){
            throw new UsernameNotFoundException("Invalid username or password");
        }
        if ( !passwordEncoder.matches( password, userDetails.getPassword()) ){
            throw new BadCredentialsException("Invalid username or password");
        }
        return new UsernamePasswordAuthenticationToken( email, userDetails.getPassword(), userDetails.getAuthorities() );
    }
    @Override
    public AuthUser findAuthUserByUsername(String username) {
        return authUserRepository.findByUsernameWithRoles(username).orElseThrow( ()-> new AuthUserNotFoundException("There is no AUTH USER with this username"));
    }

    public void validateUserCanBeInvited(String email) {

        if ( authUserRepository.existsByUsername(email) )  {
            throw new UserAlreadyExistsException("There is already an AUTH USER with this username");
        }
    }

    public boolean existsByUsername(String email) {
        return authUserRepository.existsByUsername(email);
    }

    @Override
    public void persistAuthUser(AuthUser newAuthUser) {
        authUserRepository.save(newAuthUser);
    }

    @Override
    @Transactional
    public AuthUser createAuthUserByInvitation(String email, String password, String roleName) {

        this.validateUserCanBeInvited( email );

        AuthUser newAuthUser = authUserMapper.setAttributesToAuthUser( email, password );

        this.setRoleToAuthUser( newAuthUser, roleName );

        return newAuthUser;
    }

    @Override
    public void createPlatformAdmin(String adminEmail, String adminPassword) {

        AuthUser newAdmin = authUserMapper.setAttributesToAuthUser( adminEmail, adminPassword );

        Role role = roleService.getRoleByName("ADMIN");

        newAdmin.setRoles( Set.of( role) );

        this.persistAuthUser(newAdmin);

        userProfileService.createPlatformUserProfile(newAdmin);
    }

    @Override
    public void setRoleToAuthUser(AuthUser newAuthUser, String roleName ){

        Role role = roleService.getRoleByName( roleName );

        newAuthUser.setRoles( Set.of( role) );

        this.persistAuthUser(newAuthUser);
    }

    @Override
    public AuthUser findByUsernameWithProfileAndClinic(String username) {
        return authUserRepository.findByUsernameWithProfileAndClinic(username)
                                 .orElseThrow( ()-> new AuthUserNotFoundException("There is no auth user with this username"));
    }

}
