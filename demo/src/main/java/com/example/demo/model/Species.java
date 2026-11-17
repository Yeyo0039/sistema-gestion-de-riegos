package com.example.demo.model;

import java.math.BigDecimal;

public record Species(
        Long id,
        String nombre,
        String tipo,
        String descripcion,
        BigDecimal temperaturaOptimaMinima,
        BigDecimal temperaturaOptimaMaxima,
        BigDecimal temperaturaMinima,
        BigDecimal temperaturaMaxima,
        BigDecimal humedadOptimaMinima,
        BigDecimal humedadOptimaMaxima,
        BigDecimal humedadMinima,
        BigDecimal humedadMaxima,
        String faseCrecimiento,
        Integer intervaloRiego,
        BigDecimal volumenRiego,
        String sustrato) {
}
