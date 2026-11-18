package com.example.demo.service;

import com.example.demo.model.Medicion;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

// API de entrada y consulta de lecturas de temperatura y humedad.
@RestController
@RequestMapping("/mediciones")
public class MedicionController {

    private final MedicionService service;

    public MedicionController(MedicionService service) {
        this.service = service;
    }

    // Solo recibe los datos de la lectura; el servicio calcula estados y alertas.
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Medicion crear(@RequestBody Medicion medicion) {
        validar(medicion);
        return service.registrar(medicion.getCultivoId(), medicion.getTemperatura(), medicion.getHumedad());
    }

    // Devuelve las mediciones ordenadas por fecha desde el repositorio.
    @GetMapping
    public List<Medicion> cargar() {
        return service.cargar();
    }

    // Valida rangos físicos básicos antes de consultar los umbrales del cultivo.
    private void validar(Medicion medicion) {
        if (medicion == null || medicion.getCultivoId() == null || medicion.getCultivoId() <= 0) {
            throw new IllegalArgumentException("Selecciona un cultivo válido.");
        }
        if (medicion.getTemperatura() == null || !Double.isFinite(medicion.getTemperatura())
                || medicion.getTemperatura() < -50 || medicion.getTemperatura() > 100) {
            throw new IllegalArgumentException("La temperatura debe estar entre -50 °C y 100 °C.");
        }
        if (medicion.getHumedad() == null || !Double.isFinite(medicion.getHumedad())
                || medicion.getHumedad() < 0 || medicion.getHumedad() > 100) {
            throw new IllegalArgumentException("La humedad debe estar entre 0% y 100%.");
        }
    }
}
