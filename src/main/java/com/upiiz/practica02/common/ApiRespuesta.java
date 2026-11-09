package com.upiiz.practica02.common;

import io.swagger.v3.oas.annotations.media.Schema;

// Estructura JSON común para todas las respuestas de la API
@Schema(description = "Respuesta estándar de la API")
public record ApiRespuesta<T>(
        @Schema(description = "Código de estado HTTP", example = "200") int estado,
        @Schema(description = "Mensaje descriptivo del resultado", example = "Recurso encontrado") String mensaje,
        @Schema(description = "Valores correspondientes, si aplican") T datos) {

    public static <T> ApiRespuesta<T> of(int estado, String mensaje, T datos) {
        return new ApiRespuesta<>(estado, mensaje, datos);
    }

    public static <T> ApiRespuesta<T> of(int estado, String mensaje) {
        return new ApiRespuesta<>(estado, mensaje, null);
    }
}
