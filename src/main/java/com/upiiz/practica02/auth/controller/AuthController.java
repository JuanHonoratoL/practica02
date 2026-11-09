package com.upiiz.practica02.auth.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.upiiz.practica02.auth.service.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticación", description = "Endpoints públicos para registro e inicio de sesión con generación de JWT")
public class AuthController {
    private final AuthService service;

    @Operation(
            summary = "Registrar un nuevo usuario",
            description = "Registra un nuevo usuario con rol CONSULTA por defecto y devuelve un token JWT.",
            requestBody = @RequestBody(
                    description = "Datos del usuario a registrar",
                    required = true,
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = RegisterRequest.class))),
            responses = {
                    @ApiResponse(responseCode = "201", description = "Usuario registrado correctamente y JWT generado"),
                    @ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content),
                    @ApiResponse(responseCode = "409", description = "El correo ya se encuentra registrado", content = @Content)
            })
    @PostMapping("/registro")
    public ResponseEntity<TokenResponse> register(
            @Valid @org.springframework.web.bind.annotation.RequestBody final RegisterRequest request) {
        final TokenResponse token = service.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(token);
    }

    @Operation(
            summary = "Iniciar sesión",
            description = "Autentica las credenciales del usuario y devuelve un token JWT firmado.",
            requestBody = @RequestBody(
                    description = "Credenciales de acceso",
                    required = true,
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = LoginRequest.class))),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Autenticación exitosa y JWT generado"),
                    @ApiResponse(responseCode = "400", description = "Datos de solicitud inválidos", content = @Content),
                    @ApiResponse(responseCode = "401", description = "Credenciales incorrectas", content = @Content)
            })
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> authenticate(
            @Valid @org.springframework.web.bind.annotation.RequestBody final LoginRequest request) {
        final TokenResponse token = service.login(request);
        return ResponseEntity.ok(token);
    }

    @Operation(
            summary = "Registrar un usuario ADMINISTRADOR (Requiere rol ADMIN)",
            description = "Registra un nuevo usuario con rol ADMIN. Requiere presentar un token JWT con rol ADMIN.",
            security = @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth"),
            requestBody = @RequestBody(
                    description = "Datos del administrador a registrar",
                    required = true,
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = RegisterRequest.class))),
            responses = {
                    @ApiResponse(responseCode = "201", description = "Administrador registrado correctamente"),
                    @ApiResponse(responseCode = "401", description = "No autenticado"),
                    @ApiResponse(responseCode = "403", description = "No autorizado (se requiere rol ADMIN)"),
                    @ApiResponse(responseCode = "409", description = "El correo ya se encuentra registrado")
            })
    @PostMapping("/registro-admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TokenResponse> registerAdmin(
            @Valid @org.springframework.web.bind.annotation.RequestBody final RegisterRequest request) {
        final TokenResponse token = service.registerAdmin(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(token);
    }
}
