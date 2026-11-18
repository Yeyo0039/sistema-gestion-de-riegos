package com.example.demo.controller;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

// Convierte excepciones del backend en respuestas JSON comprensibles para la interfaz.
@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(ApiExceptionHandler.class);

    // Error específico cuando PostgreSQL no está disponible o las credenciales son inválidas.
    @ExceptionHandler(CannotGetJdbcConnectionException.class)
    public ResponseEntity<Map<String, String>> handleDatabaseUnavailable(
            CannotGetJdbcConnectionException exception) {
        logger.error("No fue posible conectar con la base de datos.", exception);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of("error", "No fue posible conectar con la base de datos."));
    }

    // Respuesta uniforme para rutas o recursos que no existen.
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(NoResourceFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "El recurso solicitado no existe."));
    }

    // Errores de validación producidos por los controladores.
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleInvalidData(IllegalArgumentException exception) {
        return ResponseEntity.badRequest().body(Map.of("error", exception.getMessage()));
    }

    // El cliente envió un cuerpo vacío o un JSON con formato inválido.
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> handleUnreadableJson(HttpMessageNotReadableException exception) {
        logger.warn("El cuerpo de la solicitud no contiene JSON válido.");
        return ResponseEntity.badRequest().body(Map.of("error", "El cuerpo debe contener JSON válido."));
    }

    // Fallback para evitar exponer detalles internos al cliente.
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleUnexpectedError(Exception exception) {
        logger.error("Error inesperado al procesar la solicitud.", exception);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "No se pudo completar la solicitud."));
    }
}
