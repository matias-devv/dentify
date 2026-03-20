package com.dentify.security.service;

import com.dentify.security.dto.request.AuthLoginRequest;
import com.dentify.security.dto.response.AuthResponse;
import com.dentify.security.dto.response.LoginResult;
import com.dentify.security.model.AuthUser;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.List;
import java.util.Optional;

public interface IAuthUserService {

    LoginResult loginUser(@Valid AuthLoginRequest userRequest);

    public Authentication authenticate(String username, String password);

    public String resolveClinicTenant(AuthUser authUser);

    AuthUser findAuthUserByUsername(String username);

    void validateUserCanBeInvited(@NotBlank @Email String email);

    void persistAuthUser(AuthUser newAuthUser);

    void setRoleToAuthUser(AuthUser newAuthUser, String roleName);

    AuthUser createAuthUserByInvitation(@NotBlank @Email String email, @NotBlank @Size(min = 8) String password, String roleName);

    boolean existsByUsername(String email);

    void createPlatformAdmin(String adminEmail, String adminPassword);

    AuthUser findByUsernameWithProfileAndClinic(String username);

}
