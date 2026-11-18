package com.example.demo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// Configuración transversal de las solicitudes HTTP realizadas al backend.
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    // Permite que la interfaz local consulte los endpoints bajo /api.
    // Las peticiones de la interfaz servida por Spring usan el mismo origen,
    // pero estas reglas también permiten el acceso desde los hosts locales.
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:9090", "http://127.0.0.1:9090")
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .maxAge(3600);
    }
}
