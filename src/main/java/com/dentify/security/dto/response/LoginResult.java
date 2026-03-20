package com.dentify.security.dto.response;

import com.dentify.security.model.AuthUser;

public record LoginResult(AuthResponse response, AuthUser authUser){}