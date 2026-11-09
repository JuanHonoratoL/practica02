package com.upiiz.practica02.cursos.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

// Recibir datos del cliente
@Schema(description = "Datos para crear o actualizar un curso")
public record NuevoCursoDto(
        @Schema(description = "Nombre del curso", example = "Java con Spring Boot", maxLength = 100)
        @NotBlank @Size(max = 100) String nombre,
        @Schema(description = "Duración total en horas", example = "40")
        @NotNull @Positive Integer duracionHoras,
        @Schema(description = "Nivel del curso (básico, medio, avanzado)", example = "medio", maxLength = 50)
        @NotBlank @Size(max = 50) String nivel,
        @Schema(description = "Costo del curso", example = "1499.90")
        @NotNull @Positive @Digits(integer = 8, fraction = 2) BigDecimal precio,
        @Schema(description = "Fecha de inicio programada (yyyy-MM-dd)", example = "2026-11-02")
        @NotNull LocalDate fechaInicio) {
}
