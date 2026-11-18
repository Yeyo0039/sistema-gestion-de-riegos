package com.example.demo.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.SQLException;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.core.JdbcTemplate;

class ApiHealthControllerTest {

    @Test
    void reportsDatabaseAsDownWhenConnectionCannotBeObtained() {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        when(jdbc.queryForObject("SELECT current_database()", String.class))
                .thenThrow(new CannotGetJdbcConnectionException(
                        "Could not obtain JDBC connection", new SQLException("Connection refused")));

        ResponseEntity<Map<String, String>> response = new ApiHealthController(jdbc).health();

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertEquals("DOWN", response.getBody().get("status"));
        assertEquals("UNAVAILABLE", response.getBody().get("database"));
    }
}
