package com.example.demo.repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import com.example.demo.dto.FarmRequests.CropRequest;
import com.example.demo.dto.FarmRequests.MeasurementRequest;
import com.example.demo.dto.FarmRequests.SpeciesRequest;
import com.example.demo.model.Crop;
import com.example.demo.model.Measurement;
import com.example.demo.model.Species;

@Repository
public class FarmRepository {

    private final JdbcTemplate jdbc;

    public FarmRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Species> findSpecies() {
        return jdbc.query("""
                SELECT id, nombre, tipo, descripcion,
                       temperatura_optima_minima, temperatura_optima_maxima,
                       temperatura_minima, temperatura_maxima,
                       humedad_optima_minima, humedad_optima_maxima,
                       humedad_minima, humedad_maxima, fase_crecimiento,
                       intervalo_riego, volumen_riego, sustrato
                FROM species ORDER BY id
                """, (rs, row) -> new Species(
                rs.getLong("id"), rs.getString("nombre"), rs.getString("tipo"), rs.getString("descripcion"),
                rs.getBigDecimal("temperatura_optima_minima"), rs.getBigDecimal("temperatura_optima_maxima"),
                rs.getBigDecimal("temperatura_minima"), rs.getBigDecimal("temperatura_maxima"),
                rs.getBigDecimal("humedad_optima_minima"), rs.getBigDecimal("humedad_optima_maxima"),
                rs.getBigDecimal("humedad_minima"), rs.getBigDecimal("humedad_maxima"),
                rs.getString("fase_crecimiento"), rs.getInt("intervalo_riego"),
                rs.getBigDecimal("volumen_riego"), rs.getString("sustrato")));
    }

    public Species insertSpecies(SpeciesRequest request) {
        long id = insert("""
                INSERT INTO species (nombre, tipo, descripcion,
                    temperatura_optima_minima, temperatura_optima_maxima, temperatura_minima, temperatura_maxima,
                    humedad_optima_minima, humedad_optima_maxima, humedad_minima, humedad_maxima,
                    fase_crecimiento, intervalo_riego, volumen_riego, sustrato)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """, request.nombre(), request.tipo(), request.descripcion(),
                request.temperaturaOptimaMinima(), request.temperaturaOptimaMaxima(),
                request.temperaturaMinima(), request.temperaturaMaxima(),
                request.humedadOptimaMinima(), request.humedadOptimaMaxima(),
                request.humedadMinima(), request.humedadMaxima(), request.faseCrecimiento(),
                request.intervaloRiego(), request.volumenRiego(), request.sustrato());
        return findSpecies().stream().filter(species -> species.id() == id).findFirst().orElseThrow();
    }

    public List<Crop> findCrops() {
        return jdbc.query("""
                SELECT c.id, c.nombre, c.especie_id, s.nombre AS especie_nombre, c.fecha_inicio
                FROM crops c JOIN species s ON s.id = c.especie_id ORDER BY c.id
                """, (rs, row) -> new Crop(rs.getLong("id"), rs.getString("nombre"),
                rs.getLong("especie_id"), rs.getString("especie_nombre"),
                rs.getDate("fecha_inicio").toLocalDate()));
    }

    public Crop insertCrop(CropRequest request) {
        long id = insert("INSERT INTO crops (nombre, especie_id, fecha_inicio) VALUES (?, ?, ?)",
                request.nombre(), request.especieId(), request.fechaInicio());
        return findCrops().stream().filter(crop -> crop.id() == id).findFirst().orElseThrow();
    }

    public List<Measurement> findMeasurements() {
        return jdbc.query("""
                SELECT m.id, m.cultivo_id, c.nombre AS cultivo_nombre, m.tipo, m.valor, m.fecha
                FROM measurements m JOIN crops c ON c.id = m.cultivo_id ORDER BY m.fecha DESC, m.id DESC
                """, (rs, row) -> new Measurement(rs.getLong("id"), rs.getLong("cultivo_id"),
                rs.getString("cultivo_nombre"), rs.getString("tipo"), rs.getBigDecimal("valor"),
                rs.getTimestamp("fecha").toLocalDateTime()));
    }

    public Measurement insertMeasurement(MeasurementRequest request) {
        long id = insert("INSERT INTO measurements (cultivo_id, tipo, valor) VALUES (?, ?, ?)",
                request.cultivoId(), request.tipo(), request.valor());
        return findMeasurements().stream().filter(measurement -> measurement.id() == id).findFirst()
                .orElseThrow();
    }

    public boolean speciesExists(long id) {
        return Boolean.TRUE.equals(jdbc.queryForObject(
                "SELECT EXISTS (SELECT 1 FROM species WHERE id = ?)", Boolean.class, id));
    }

    public boolean cropExists(long id) {
        return Boolean.TRUE.equals(jdbc.queryForObject(
                "SELECT EXISTS (SELECT 1 FROM crops WHERE id = ?)", Boolean.class, id));
    }

    public void checkConnection() {
        jdbc.queryForObject("SELECT 1", Integer.class);
    }

    private long insert(String sql, Object... values) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            for (int i = 0; i < values.length; i++) {
                if (values[i] instanceof java.time.LocalDate date) {
                    statement.setDate(i + 1, java.sql.Date.valueOf(date));
                } else if (values[i] instanceof java.time.LocalDateTime dateTime) {
                    statement.setTimestamp(i + 1, Timestamp.valueOf(dateTime));
                } else {
                    statement.setObject(i + 1, values[i]);
                }
            }
            return statement;
        }, keyHolder);
        Number key = keyHolder.getKey();
        if (key == null) {
            throw new IllegalStateException("La base de datos no devolvió el identificador generado.");
        }
        return key.longValue();
    }
}
