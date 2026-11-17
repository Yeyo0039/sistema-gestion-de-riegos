package com.example.demo.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.example.demo.dto.FarmRequests.CropRequest;
import com.example.demo.dto.FarmRequests.MeasurementRequest;
import com.example.demo.dto.FarmRequests.SpeciesRequest;
import com.example.demo.model.Crop;
import com.example.demo.model.Measurement;
import com.example.demo.model.Species;
import com.example.demo.repository.FarmRepository;

@Service
public class FarmService {

    private static final Set<String> SPECIES_TYPES = Set.of("vegetal", "fruta", "flor", "otro");
    private static final Set<String> GROWTH_PHASES = Set.of(
            "germinacion", "vegetativa", "floracion", "fructificacion");
    private static final Set<String> SUBSTRATES = Set.of(
            "arenoso", "arcilloso", "franco", "turba", "compost", "fibra-de-coco");

    private final FarmRepository repository;

    public FarmService(FarmRepository repository) {
        this.repository = repository;
    }

    public List<Species> findSpecies() {
        return repository.findSpecies();
    }

    public Species createSpecies(SpeciesRequest request) {
        require(request != null, "Los datos de la especie son obligatorios.");
        requireText(request.nombre(), "nombre", 100);
        requireChoice(request.tipo(), SPECIES_TYPES, "tipo");
        requireText(request.descripcion(), "descripcion", 500);
        requireChoice(request.faseCrecimiento(), GROWTH_PHASES, "faseCrecimiento");
        requireChoice(request.sustrato(), SUBSTRATES, "sustrato");
        require(request.intervaloRiego() != null && request.intervaloRiego() > 0,
                "intervaloRiego debe ser mayor que cero.");
        requireRange(request.volumenRiego(), BigDecimal.ZERO, new BigDecimal("1000"), "volumenRiego");

        BigDecimal tempMin = requireRange(request.temperaturaMinima(), BigDecimal.ZERO,
                new BigDecimal("100"), "temperaturaMinima");
        BigDecimal tempOptimalMin = requireRange(request.temperaturaOptimaMinima(), BigDecimal.ZERO,
                new BigDecimal("100"), "temperaturaOptimaMinima");
        BigDecimal tempOptimalMax = requireRange(request.temperaturaOptimaMaxima(), BigDecimal.ZERO,
                new BigDecimal("100"), "temperaturaOptimaMaxima");
        BigDecimal tempMax = requireRange(request.temperaturaMaxima(), BigDecimal.ZERO,
                new BigDecimal("100"), "temperaturaMaxima");
        require(tempMin.compareTo(tempOptimalMin) <= 0
                        && tempOptimalMin.compareTo(tempOptimalMax) <= 0
                        && tempOptimalMax.compareTo(tempMax) <= 0,
                "Los rangos de temperatura deben estar ordenados de mínimo a máximo.");

        BigDecimal humidityMin = requireRange(request.humedadMinima(), BigDecimal.ZERO,
                new BigDecimal("100"), "humedadMinima");
        BigDecimal humidityOptimalMin = requireRange(request.humedadOptimaMinima(), BigDecimal.ZERO,
                new BigDecimal("100"), "humedadOptimaMinima");
        BigDecimal humidityOptimalMax = requireRange(request.humedadOptimaMaxima(), BigDecimal.ZERO,
                new BigDecimal("100"), "humedadOptimaMaxima");
        BigDecimal humidityMax = requireRange(request.humedadMaxima(), BigDecimal.ZERO,
                new BigDecimal("100"), "humedadMaxima");
        require(humidityMin.compareTo(humidityOptimalMin) <= 0
                        && humidityOptimalMin.compareTo(humidityOptimalMax) <= 0
                        && humidityOptimalMax.compareTo(humidityMax) <= 0,
                "Los rangos de humedad deben estar ordenados de mínimo a máximo.");
        return repository.insertSpecies(request);
    }

    public List<Crop> findCrops() {
        return repository.findCrops();
    }

    public Crop createCrop(CropRequest request) {
        require(request != null, "Los datos del cultivo son obligatorios.");
        requireText(request.nombre(), "nombre", 100);
        require(request.especieId() != null && request.especieId() > 0
                        && repository.speciesExists(request.especieId()),
                "La especie seleccionada no existe.");
        require(request.fechaInicio() != null, "fechaInicio es obligatoria.");
        return repository.insertCrop(request);
    }

    public List<Measurement> findMeasurements() {
        return repository.findMeasurements();
    }

    public Measurement createMeasurement(MeasurementRequest request) {
        require(request != null, "Los datos de la medición son obligatorios.");
        require(request.cultivoId() != null && request.cultivoId() > 0
                        && repository.cropExists(request.cultivoId()),
                "El cultivo seleccionado no existe.");
        require(request.tipo() != null
                        && Set.of("temperatura", "humedad").contains(request.tipo().toLowerCase()),
                "tipo debe ser temperatura o humedad.");
        requireRange(request.valor(), BigDecimal.ZERO, new BigDecimal("1000"), "valor");
        if ("humedad".equalsIgnoreCase(request.tipo())) {
            require(request.valor().compareTo(new BigDecimal("100")) <= 0,
                    "El valor de humedad no puede superar 100.");
        }
        return repository.insertMeasurement(request);
    }

    public void checkConnection() {
        repository.checkConnection();
    }

    private static void requireText(String value, String field, int maxLength) {
        require(value != null && !value.isBlank() && value.length() <= maxLength,
                field + " es obligatorio y no debe superar " + maxLength + " caracteres.");
    }

    private static void requireChoice(String value, Set<String> options, String field) {
        require(value != null && options.contains(value), field + " no contiene una opción válida.");
    }

    private static BigDecimal requireRange(BigDecimal value, BigDecimal min, BigDecimal max, String field) {
        require(value != null && value.compareTo(min) >= 0 && value.compareTo(max) <= 0,
                field + " debe estar entre " + min + " y " + max + ".");
        return value;
    }

    private static void require(boolean valid, String message) {
        if (!valid) {
            throw new IllegalArgumentException(message);
        }
    }
}
