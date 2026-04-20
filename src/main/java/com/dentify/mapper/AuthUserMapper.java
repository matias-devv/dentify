package com.dentify.mapper;

import com.dentify.security.model.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthUserMapper {

    private final PasswordEncoder passwordEncoder;

    public AuthUser setAttributesToAuthUser(String name, String password) {
        return AuthUser.builder()
                .username(name)
                .password(passwordEncoder.encode(password))
                .enabled(true)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialNonExpired(true)
                .build();
    }
}
