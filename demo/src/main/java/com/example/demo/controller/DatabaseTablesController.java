package com.example.demo.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.repository.DatabaseTablesRepository;

@RestController
@RequestMapping("/api/tablas")
public class DatabaseTablesController {

    private final DatabaseTablesRepository repository;

    public DatabaseTablesController(DatabaseTablesRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public Map<String, List<Map<String, Object>>> getAllTables() {
        return repository.findAllTables();
    }

    @GetMapping("/{tableName}")
    public List<Map<String, Object>> getTable(@PathVariable String tableName) {
        return repository.findTable(tableName);
    }
}
