package com.example.demo.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public final class FarmRequests {

    private FarmRequests() {
    }

    public record SpeciesRequest(
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

    public record CropRequest(String nombre, Long especieId, LocalDate fechaInicio) {
    }

    public record MeasurementRequest(Long cultivoId, String tipo, BigDecimal valor) {
    }
}
