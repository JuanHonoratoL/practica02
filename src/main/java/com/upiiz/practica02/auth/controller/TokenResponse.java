package com.upiiz.practica02.auth.controller;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Respuesta con el token JWT generado")
public record TokenResponse(
        @Schema(description = "Token de acceso JWT firmado", example = "eyJhbGciOiJIUzI1NiJ9...")
        @JsonProperty("access_token") String accessToken) {
}
