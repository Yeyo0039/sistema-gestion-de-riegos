package com.example.demo.repository;

import com.example.demo.model.Especie;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

// Acceso JDBC a las tablas relacionadas con una especie.
@Repository
public class EspecieRepository {

    private final JdbcTemplate jdbc;

    public EspecieRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // Guarda la especie, sus umbrales y la etapa de crecimiento como una operación única.
    @Transactional
    public Especie guardar(Especie especie) {
        
        Integer tipoId = guardarTipo(especie.getTipo());

        Integer especieId = jdbc.queryForObject("""
                INSERT INTO especie (nombre, tipo_id, descripcion)
                VALUES (?, ?, ?)
                RETURNING id
                """, Integer.class,
                especie.getNombre(), tipoId, especie.getDescripcion());

        jdbc.update("""
                INSERT INTO temperatura (
                    especie_id, optima_minima, optima_maxima,
                    maximo_permitido, minimo_permitido
                ) VALUES (?, ?, ?, ?, ?)
                """,
                especieId,
                especie.getTemperaturaOptimaMinima(),
                especie.getTemperaturaOptimaMaxima(),
                especie.getTemperaturaMaxima(),
                especie.getTemperaturaMinima());

        jdbc.update("""
                INSERT INTO humedad (
                    id, especie_id, optima_minima, optima_maxima,
                    maximo_permitido, minimo_permitido
                ) VALUES (
                    nextval('humedad_id_seq'), ?, ?, ?, ?, ?
                )
                """,
                especieId,
                especie.getHumedadOptimaMinima(),
                especie.getHumedadOptimaMaxima(),
                especie.getHumedadMaxima(),
                especie.getHumedadMinima());

        Integer sustratoId = guardarSustrato(especie.getSustrato());

        jdbc.update("""
                INSERT INTO etapa (
                    especie_id, tipo_sustrato_id, fase, intervalo, volumen
                ) VALUES (?, ?, ?, ?, ?)
                """,
                especieId,
                sustratoId,
                especie.getFaseCrecimiento(),
                especie.getIntervaloRiego(),
                especie.getVolumenRiego().intValue());

        especie.setId(especieId.longValue());
        return especie;
    }

    // Consulta únicamente los datos necesarios para el selector de la interfaz.
    public List<Especie> cargar() {
        return jdbc.query("SELECT id, nombre FROM especie ORDER BY nombre", (result, rowNum) -> {
            Especie especie = new Especie();
            especie.setId(result.getLong("id"));
            especie.setNombre(result.getString("nombre"));
            return especie;
        });
    }

    private Integer guardarTipo(String tipoFormulario) {
        String nombreTipo = switch (tipoFormulario) {
            case "vegetal" -> "Hortaliza";
            case "fruta" -> "Frutal";
            case "flor" -> "Aromatica";
            default -> "Tuberculo";
        };

        return jdbc.queryForObject("""
                INSERT INTO tipo (nombre)
                VALUES (?)
                ON CONFLICT (nombre) DO UPDATE SET nombre = EXCLUDED.nombre
                RETURNING id
                """, Integer.class, nombreTipo);
    }

    private Integer guardarSustrato(String sustratoFormulario) {
        if (sustratoFormulario == null || sustratoFormulario.isBlank()) {
            return null;
        }

        String nombreSustrato = switch (sustratoFormulario) {
            case "arenoso" -> "Arenoso";
            case "arcilloso" -> "Arcilloso";
            case "franco" -> "Franco";
            case "turba" -> "Turba";
            case "compost" -> "Compost";
            default -> "Fibra de coco";
        };

        return jdbc.queryForObject("""
                INSERT INTO tipo_sustrato (nombre)
                VALUES (?)
                ON CONFLICT (nombre) DO UPDATE SET nombre = EXCLUDED.nombre
                RETURNING id
                """, Integer.class, nombreSustrato);
    }
}
