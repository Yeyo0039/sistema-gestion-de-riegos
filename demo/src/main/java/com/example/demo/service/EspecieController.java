package com.example.demo.service;

import com.example.demo.model.Especie;
import com.example.demo.repository.EspecieRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.ResponseStatus;



// API para registrar y consultar las especies y sus rangos de riego.
@RestController
@RequestMapping("/especies")
public class EspecieController {

    private final EspecieRepository repository;

    public EspecieController(EspecieRepository repository) {
        this.repository = repository;
    }

    // Valida los rangos antes de delegar la persistencia al repositorio.
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Especie crear(@RequestBody Especie especie) {
        validar(especie);
        return repository.guardar(especie);
    }

    // Devuelve las especies disponibles para los selectores de la interfaz.
    @GetMapping
    public List<Especie> cargar() {
        return repository.cargar();
    }



    // Comprueba que los límites permitan clasificar correctamente una medición.
    private void validar(Especie especie) {
        validarOrden("Temperatura", especie.getTemperaturaMinima(),
                especie.getTemperaturaOptimaMinima(), especie.getTemperaturaOptimaMaxima(),
                especie.getTemperaturaMaxima());
        validarOrden("Humedad", especie.getHumedadMinima(),
                especie.getHumedadOptimaMinima(), especie.getHumedadOptimaMaxima(),
                especie.getHumedadMaxima());

        if (especie.getHumedadMinima() < 0 || especie.getHumedadMaxima() > 100
                || especie.getHumedadOptimaMinima() < 0 || especie.getHumedadOptimaMaxima() > 100) {
            throw new IllegalArgumentException("La humedad debe estar comprendida entre 0% y 100%.");
        }

        if (especie.getTemperaturaMinima() == null
                || especie.getTemperaturaMaxima() == null
                || especie.getHumedadMinima() == null
                || especie.getHumedadMaxima() == null) {
            throw new IllegalArgumentException("Los rangos de temperatura y humedad son obligatorios.");
        }
    }

    private void validarOrden(String nombre, Double... valores) {
        for (Double valor : valores) {
            if (valor == null) {
                throw new IllegalArgumentException(nombre + ": todos los rangos son obligatorios.");
            }
        }

        if (valores[0] > valores[1] || valores[1] > valores[2] || valores[2] > valores[3]) {
            throw new IllegalArgumentException(
                    nombre + ": el mínimo permitido debe ser menor o igual al óptimo mínimo, "
                            + "el óptimo máximo menor o igual al máximo permitido.");
        }
    }
}
