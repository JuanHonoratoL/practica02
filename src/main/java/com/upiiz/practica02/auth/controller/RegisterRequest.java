package com.upiiz.practica02.auth.controller;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Datos para registrar un nuevo usuario (asigna rol CONSULTA)")
public record RegisterRequest(
        @Schema(description = "Correo electrónico del usuario", example = "consulta@ipn.mx")
        @NotBlank @Email String email,
        @Schema(description = "Contraseña del usuario (mínimo 6 caracteres)", example = "consulta123")
        @NotBlank @Size(min = 6) String password,
        @Schema(description = "Nombre completo del usuario", example = "Juan Pérez")
        @NotBlank String name) {
}
