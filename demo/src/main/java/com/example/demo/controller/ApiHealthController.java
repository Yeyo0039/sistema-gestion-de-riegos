package com.example.demo.controller;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ApiHealthController {

    private final JdbcTemplate jdbc;

    public ApiHealthController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @GetMapping("/api/health")
    public FarmController.HealthResponse health() {
        String database = jdbc.queryForObject("SELECT current_database()", String.class);
        return new FarmController.HealthResponse("UP", database);
    }
}
