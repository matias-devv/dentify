package com.dentify.security.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record AuthLoginRequest(@NotBlank(message = "El correo no puede estar vacío")
                               @Email(message = "Formato de correo inválido")
                               String email,

                               @NotBlank(message = "La contraseña no puede estar vacía")
                               String password) {
}
