package com.example.demo.service;

import com.example.demo.model.Cultivo;
import com.example.demo.repository.CultivoRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

// API para crear cultivos asociados a una especie y consultar los existentes.
@RestController
@RequestMapping("/cultivos")
public class CultivoController {

    private final CultivoRepository repository;

    public CultivoController(CultivoRepository repository) {
        this.repository = repository;
    }

    // Valida y normaliza el nombre antes de guardarlo.
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Cultivo crear(@RequestBody Cultivo cultivo) {
        validar(cultivo);
        cultivo.setNombre(cultivo.getNombre().trim());
        return repository.guardar(cultivo);
    }

    // Lista los cultivos que pueden recibir nuevas mediciones.
    @GetMapping
    public List<Cultivo> cargar() {
        return repository.cargar();
    }

    private void validar(Cultivo cultivo) {
        if (cultivo == null || cultivo.getNombre() == null
                || cultivo.getNombre().trim().length() < 10
                || cultivo.getNombre().trim().length() > 100) {
            throw new IllegalArgumentException("El nombre del cultivo debe tener entre 10 y 100 caracteres.");
        }

        if (cultivo.getEspecieId() == null || cultivo.getEspecieId() <= 0) {
            throw new IllegalArgumentException("Selecciona una especie válida.");
        }

        if (cultivo.getFechaInicio() == null) {
            throw new IllegalArgumentException("La fecha de inicio es obligatoria.");
        }
    }
}
