package com.upiiz.practica02.cursos.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;

// Contestar al cliente
@Schema(description = "Curso devuelto por la API")
public record CursoDto(
        @Schema(description = "Identificador del curso", example = "1") Long id,
        @Schema(description = "Nombre del curso", example = "Java con Spring Boot") String nombre,
        @Schema(description = "Duración total en horas", example = "40") Integer duracionHoras,
        @Schema(description = "Nivel del curso (básico, medio, avanzado)", example = "medio") String nivel,
        @Schema(description = "Costo del curso", example = "1499.90") BigDecimal precio,
        @Schema(description = "Fecha de inicio programada", example = "2026-11-02") LocalDate fechaInicio) {
}
