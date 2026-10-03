package com.universidad.tareas.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class TareaNoEncontradaException extends RuntimeException {
    public TareaNoEncontradaException(String message) {
        super(message);
    }
}
