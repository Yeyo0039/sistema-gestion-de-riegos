package com.example.demo.model;

import java.time.LocalDate;

public record Crop(Long id, String nombre, Long especieId, String especieNombre, LocalDate fechaInicio) {
}
