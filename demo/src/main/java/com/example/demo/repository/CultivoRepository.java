package com.example.demo.repository;

import com.example.demo.model.Cultivo;
import java.sql.Date;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

// Acceso JDBC a la tabla cultivo y validación de sus relaciones.
@Repository
public class CultivoRepository {

    private final JdbcTemplate jdbc;

    public CultivoRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // Comprueba la especie y persiste el cultivo dentro de una transacción.
    @Transactional
    public Cultivo guardar(Cultivo cultivo) {
        Boolean especieExiste = jdbc.queryForObject(
            "SELECT EXISTS (SELECT 1 FROM especie WHERE id = ?)",
            Boolean.class,
            Math.toIntExact(cultivo.getEspecieId()));
        if (!Boolean.TRUE.equals(especieExiste)) {
            throw new IllegalArgumentException("La especie seleccionada no existe.");
        }

        Integer cultivoId = jdbc.queryForObject(
            "SELECT COALESCE(MAX(id), 0) + 1 FROM cultivo", Integer.class);

        jdbc.update("""
            INSERT INTO cultivo (id, nombre, especie, fecha_inicio)
                VALUES (?, ?, ?, ?)
                """,
                cultivoId,
                cultivo.getNombre(),
                Math.toIntExact(cultivo.getEspecieId()),
                cultivo.getFechaInicio());

        cultivo.setId(cultivoId.longValue());
        return cultivo;
    }

    // Recupera los cultivos ordenados por nombre para mostrarlos en la UI.
    public List<Cultivo> cargar() {
        return jdbc.query("SELECT id, nombre, especie, fecha_inicio FROM cultivo ORDER BY nombre", (result, rowNum) -> {
            Cultivo cultivo = new Cultivo();
            cultivo.setId(result.getLong("id"));
            cultivo.setNombre(result.getString("nombre"));
            cultivo.setEspecieId(result.getLong("especie"));
            Date fechaInicio = result.getDate("fecha_inicio");
            cultivo.setFechaInicio(fechaInicio == null ? null : fechaInicio.toLocalDate());
            return cultivo;
        });
    }
}
