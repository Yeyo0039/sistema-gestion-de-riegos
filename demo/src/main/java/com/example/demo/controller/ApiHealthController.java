package com.example.demo.controller;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

// Endpoint técnico para comprobar que la aplicación y PostgreSQL están disponibles.
@RestController
public class ApiHealthController {

    private static final Logger logger = LoggerFactory.getLogger(ApiHealthController.class);

    private final JdbcTemplate jdbc;

    public ApiHealthController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @GetMapping("/api/health")
    // Devuelve 200 cuando la consulta de salud funciona y 503 cuando falla la conexión.
    public ResponseEntity<Map<String, String>> health() {
        try {
            String database = jdbc.queryForObject("SELECT current_database()", String.class);
            return ResponseEntity.ok(Map.of("status", "UP", "database", database));
        } catch (CannotGetJdbcConnectionException exception) {
            logger.warn("La comprobación de salud no pudo conectar con PostgreSQL.", exception);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of(
                    "status", "DOWN",
                    "database", "UNAVAILABLE",
                    "error", "No fue posible conectar con PostgreSQL. Verifique la URL, el usuario y la autenticación."));
        }
    }

}
