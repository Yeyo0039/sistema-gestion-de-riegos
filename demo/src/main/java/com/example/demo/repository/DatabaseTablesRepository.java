package com.example.demo.repository;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class DatabaseTablesRepository {

    private static final List<String> TABLE_NAMES = List.of(
            "tipo", "tipo_sustrato", "especie", "temperatura", "etapa");

    private final JdbcTemplate jdbc;

    public DatabaseTablesRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Map<String, List<Map<String, Object>>> findAllTables() {
        Map<String, List<Map<String, Object>>> tables = new LinkedHashMap<>();
        TABLE_NAMES.forEach(tableName -> tables.put(tableName, findTable(tableName)));
        return tables;
    }

    public List<Map<String, Object>> findTable(String tableName) {
        if (!TABLE_NAMES.contains(tableName)) {
            throw new IllegalArgumentException("La tabla solicitada no está disponible.");
        }
        return jdbc.queryForList("SELECT * FROM public." + tableName + " ORDER BY id");
    }
}
