package com.example.demo.repository;

import com.example.demo.model.Alerta;
import com.example.demo.model.Medicion;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

// Acceso JDBC a mediciones, umbrales y alertas asociadas.
@Repository
public class MedicionRepository {

    private final JdbcTemplate jdbc;

    public MedicionRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // Agrupa los límites que el servicio necesita para clasificar una lectura.
    public record Umbrales(
            Double temperaturaMinima,
            Double temperaturaOptimaMinima,
            Double temperaturaOptimaMaxima,
            Double temperaturaMaxima,
            Double humedadMinima,
            Double humedadOptimaMinima,
            Double humedadOptimaMaxima,
            Double humedadMaxima) {
    }

    // Obtiene los límites de temperatura y humedad de la especie del cultivo.
    public Umbrales cargarUmbrales(Long cultivoId) {
        Boolean cultivoExiste = jdbc.queryForObject(
                "SELECT EXISTS (SELECT 1 FROM cultivo WHERE id = ?)",
                Boolean.class,
                Math.toIntExact(cultivoId));
        if (!Boolean.TRUE.equals(cultivoExiste)) {
            throw new IllegalArgumentException("El cultivo seleccionado no existe.");
        }

        return jdbc.queryForObject("""
                SELECT t.minimo_permitido AS temperatura_minima,
                       t.optima_minima AS temperatura_optima_minima,
                       t.optima_maxima AS temperatura_optima_maxima,
                       t.maximo_permitido AS temperatura_maxima,
                       h.minimo_permitido AS humedad_minima,
                       h.optima_minima AS humedad_optima_minima,
                       h.optima_maxima AS humedad_optima_maxima,
                       h.maximo_permitido AS humedad_maxima
                FROM cultivo c
                JOIN temperatura t ON t.especie_id = c.especie
                JOIN humedad h ON h.especie_id = c.especie
                WHERE c.id = ?
                """, (result, rowNum) -> new Umbrales(
                result.getDouble("temperatura_minima"),
                result.getDouble("temperatura_optima_minima"),
                result.getDouble("temperatura_optima_maxima"),
                result.getDouble("temperatura_maxima"),
                result.getDouble("humedad_minima"),
                result.getDouble("humedad_optima_minima"),
                result.getDouble("humedad_optima_maxima"),
                result.getDouble("humedad_maxima")), Math.toIntExact(cultivoId));
    }

    // Inserta la medición y sus alertas para evitar resultados parciales.
    @Transactional
    public Medicion guardar(Medicion medicion) {
        Integer medicionId = jdbc.queryForObject("SELECT nextval('medicion_id_seq')", Integer.class);
        jdbc.update("""
                INSERT INTO medicion (
                    id, cultivo_id, temperatura, humedad, fecha_hora,
                    estado_temperatura, mensaje_temperatura,
                    estado_humedad, mensaje_humedad, estado_general, mensaje
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                medicionId,
                Math.toIntExact(medicion.getCultivoId()),
                medicion.getTemperatura(),
                medicion.getHumedad(),
                Timestamp.valueOf(medicion.getFechaHora()),
                medicion.getEstadoTemperatura(),
                medicion.getMensajeTemperatura(),
                medicion.getEstadoHumedad(),
                medicion.getMensajeHumedad(),
                medicion.getEstadoGeneral(),
                medicion.getMensaje());

        medicion.setId(medicionId.longValue());
        for (Alerta alerta : medicion.getAlertas()) {
            Integer alertaId = jdbc.queryForObject("SELECT nextval('alerta_id_seq')", Integer.class);
            jdbc.update("""
                    INSERT INTO alerta (id, medicion_id, variable, estado, mensaje)
                    VALUES (?, ?, ?, ?, ?)
                    """,
                    alertaId,
                    medicionId,
                    alerta.getVariable(),
                    alerta.getEstado(),
                    alerta.getMensaje());
            alerta.setId(alertaId.longValue());
        }
        return medicion;
    }

    // Usa un extractor para reconstruir una medición con todas sus alertas.
    public List<Medicion> cargar() {
        return jdbc.query("""
                SELECT m.id, m.cultivo_id, c.nombre AS cultivo_nombre,
                       m.temperatura, m.humedad, m.fecha_hora,
                       m.estado_temperatura, m.mensaje_temperatura,
                       m.estado_humedad, m.mensaje_humedad,
                       m.estado_general, m.mensaje,
                       a.id AS alerta_id, a.variable AS alerta_variable,
                       a.estado AS alerta_estado, a.mensaje AS alerta_mensaje
                FROM medicion m
                JOIN cultivo c ON c.id = m.cultivo_id
                LEFT JOIN alerta a ON a.medicion_id = m.id
                ORDER BY m.fecha_hora DESC, m.id DESC, a.id
                """, (ResultSetExtractor<List<Medicion>>) result -> {
            Map<Long, Medicion> mediciones = new LinkedHashMap<>();
            while (result.next()) {
                Long id = result.getLong("id");
                Medicion medicion = mediciones.computeIfAbsent(id, key -> {
                    try {
                        Medicion nueva = new Medicion();
                        nueva.setId(id);
                        nueva.setCultivoId(result.getLong("cultivo_id"));
                        nueva.setCultivoNombre(result.getString("cultivo_nombre"));
                        nueva.setTemperatura(result.getDouble("temperatura"));
                        nueva.setHumedad(result.getDouble("humedad"));
                        nueva.setFechaHora(result.getTimestamp("fecha_hora").toLocalDateTime());
                        nueva.setEstadoTemperatura(result.getString("estado_temperatura"));
                        nueva.setMensajeTemperatura(result.getString("mensaje_temperatura"));
                        nueva.setEstadoHumedad(result.getString("estado_humedad"));
                        nueva.setMensajeHumedad(result.getString("mensaje_humedad"));
                        nueva.setEstadoGeneral(result.getString("estado_general"));
                        nueva.setMensaje(result.getString("mensaje"));
                        return nueva;
                    } catch (java.sql.SQLException exception) {
                        throw new IllegalStateException(exception);
                    }
                });

                Number alertaIdValue = (Number) result.getObject("alerta_id");
                if (alertaIdValue != null) {
                    Alerta alerta = new Alerta();
                    alerta.setId(alertaIdValue.longValue());
                    alerta.setVariable(result.getString("alerta_variable"));
                    alerta.setEstado(result.getString("alerta_estado"));
                    alerta.setMensaje(result.getString("alerta_mensaje"));
                    medicion.getAlertas().add(alerta);
                }
            }
            return new ArrayList<>(mediciones.values());
        });
    }
}
