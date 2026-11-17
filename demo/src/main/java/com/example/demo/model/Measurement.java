package com.example.demo.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record Measurement(Long id, Long cultivoId, String cultivoNombre, String tipo,
        BigDecimal valor, LocalDateTime fecha) {
}
