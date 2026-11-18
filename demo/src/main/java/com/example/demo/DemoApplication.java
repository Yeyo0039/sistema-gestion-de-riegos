package com.example.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.core.JdbcTemplate;

// Punto de entrada de la aplicación y configuración principal de Spring Boot.
@SpringBootApplication
public class DemoApplication {

    // Logger usado para informar si la base de datos está disponible al arrancar.
	private static final Logger logger = LoggerFactory.getLogger(DemoApplication.class);

	// Spring Boot crea el contexto, configura la API y arranca el servidor HTTP.
	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

	// Ejecuta una consulta sencilla después de iniciar la aplicación para detectar
	// problemas de URL, credenciales o disponibilidad de PostgreSQL.
	@Bean
	ApplicationRunner checkDatabaseConnectionOnStartup(JdbcTemplate jdbc) {
		return args -> {
			try {
				jdbc.queryForObject("SELECT 1", Integer.class);
				logger.info("Estado inicial de PostgreSQL: UP");
			} catch (CannotGetJdbcConnectionException exception) {
				logger.warn("Estado inicial de PostgreSQL: DOWN", exception);
			}
		};
	}

}
 
