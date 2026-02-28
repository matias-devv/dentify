package com.dentify.security.service;

import com.dentify.security.dto.request.AuthLoginRequest;
import com.dentify.security.dto.response.AuthResponse;
import com.dentify.security.model.AuthUser;
import jakarta.validation.Valid;

public interface IAuthService {

    public AuthResponse loginUser(@Valid AuthLoginRequest userRequest);


    AuthUser findAuthUseryByUsername(String username);

}
