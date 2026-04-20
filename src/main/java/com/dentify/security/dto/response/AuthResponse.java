package com.dentify.security.dto.response;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"username","message","jwt","status"})
public record AuthResponse(String username, String message, String jwt, String tenantId, boolean status) {
}
