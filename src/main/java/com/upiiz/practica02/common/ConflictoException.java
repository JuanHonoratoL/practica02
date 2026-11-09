package com.upiiz.practica02.common;

// 409: el recurso ya existe (duplicado)
public class ConflictoException extends RuntimeException {
    public ConflictoException(String mensaje) {
        super(mensaje);
    }
}
