package com.dentify.security.service;

import com.dentify.common.util.JwtUtils;
import com.dentify.security.dto.request.AuthLoginRequest;
import com.dentify.security.dto.response.AuthResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService implements IAuthService{

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    @Override
    public AuthResponse loginUser(AuthLoginRequest userRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        userRequest.username(), userRequest.password()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String token = jwtUtils.createToken(authentication);

        return new AuthResponse( userRequest.username(), "login successful", token, true);
    }
}
