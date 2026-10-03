package com.universidad.tareas.exception;

import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class ApiErrorHandler {

    // Maneja los errores de validación de los controladores REST y devuelve un JSON en lugar de una página HTML.
    // Comentario: La vista Thymeleaf en TareaController utiliza el objeto BindingResult para capturar
    // los errores e inyectarlos de vuelta en la plantilla HTML para su renderizado.
    // Para la API REST, en cambio, usamos @RestControllerAdvice y capturamos MethodArgumentNotValidException
    // para transformar los errores en una estructura JSON clara y fácilmente consumible por el cliente.
    // Ambos mecanismos coexisten a propósito para servir a dos consumidores distintos (Navegador vs API Client).
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Map<String, String> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return errors;
    }
}
