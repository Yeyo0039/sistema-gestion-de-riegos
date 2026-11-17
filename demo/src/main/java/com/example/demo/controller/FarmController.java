package com.example.demo.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.FarmRequests.CropRequest;
import com.example.demo.dto.FarmRequests.MeasurementRequest;
import com.example.demo.dto.FarmRequests.SpeciesRequest;
import com.example.demo.model.Crop;
import com.example.demo.model.Measurement;
import com.example.demo.model.Species;
import com.example.demo.service.FarmService;

@RestController
@RequestMapping("/api")
public class FarmController {

    private final FarmService service;

    public FarmController(FarmService service) {
        this.service = service;
    }

    @GetMapping("/species")
    public List<Species> getSpecies() {
        return service.findSpecies();
    }

    @PostMapping("/species")
    @ResponseStatus(HttpStatus.CREATED)
    public Species createSpecies(@RequestBody SpeciesRequest request) {
        return service.createSpecies(request);
    }

    @GetMapping("/crops")
    public List<Crop> getCrops() {
        return service.findCrops();
    }

    @PostMapping("/crops")
    @ResponseStatus(HttpStatus.CREATED)
    public Crop createCrop(@RequestBody CropRequest request) {
        return service.createCrop(request);
    }

    @GetMapping("/measurements")
    public List<Measurement> getMeasurements() {
        return service.findMeasurements();
    }

    @PostMapping("/measurements")
    @ResponseStatus(HttpStatus.CREATED)
    public Measurement createMeasurement(@RequestBody MeasurementRequest request) {
        return service.createMeasurement(request);
    }

    public record HealthResponse(String status, String database) {
    }
}
