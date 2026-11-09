package com.upiiz.practica02.auth.controller;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Credenciales para iniciar sesión")
public record LoginRequest(
        @Schema(description = "Correo electrónico del usuario", example = "admin@ipn.mx")
        @NotBlank @Email String email,
        @Schema(description = "Contraseña del usuario", example = "admin123")
        @NotBlank String password) {
}
