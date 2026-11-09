package com.upiiz.practica02.common;

// 400: datos o parámetros inválidos
public class SolicitudInvalidaException extends RuntimeException {
    public SolicitudInvalidaException(String mensaje) {
        super(mensaje);
    }
}
