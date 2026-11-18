package com.example.demo.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// Lectura de un cultivo, su clasificación y las alertas resultantes.
public class Medicion {

    private Long id;
    private Long cultivoId;
    private String cultivoNombre;
    private Double temperatura;
    private Double humedad;
    private LocalDateTime fechaHora;
    private String estadoTemperatura;
    private String mensajeTemperatura;
    private String estadoHumedad;
    private String mensajeHumedad;
    private String estadoGeneral;
    private String mensaje;
    private List<Alerta> alertas = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCultivoId() {
        return cultivoId;
    }

    public void setCultivoId(Long cultivoId) {
        this.cultivoId = cultivoId;
    }

    public String getCultivoNombre() {
        return cultivoNombre;
    }

    public void setCultivoNombre(String cultivoNombre) {
        this.cultivoNombre = cultivoNombre;
    }

    public Double getTemperatura() {
        return temperatura;
    }

    public void setTemperatura(Double temperatura) {
        this.temperatura = temperatura;
    }

    public Double getHumedad() {
        return humedad;
    }

    public void setHumedad(Double humedad) {
        this.humedad = humedad;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    public String getEstadoTemperatura() {
        return estadoTemperatura;
    }

    public void setEstadoTemperatura(String estadoTemperatura) {
        this.estadoTemperatura = estadoTemperatura;
    }

    public String getMensajeTemperatura() {
        return mensajeTemperatura;
    }

    public void setMensajeTemperatura(String mensajeTemperatura) {
        this.mensajeTemperatura = mensajeTemperatura;
    }

    public String getEstadoHumedad() {
        return estadoHumedad;
    }

    public void setEstadoHumedad(String estadoHumedad) {
        this.estadoHumedad = estadoHumedad;
    }

    public String getMensajeHumedad() {
        return mensajeHumedad;
    }

    public void setMensajeHumedad(String mensajeHumedad) {
        this.mensajeHumedad = mensajeHumedad;
    }

    public String getEstadoGeneral() {
        return estadoGeneral;
    }

    public void setEstadoGeneral(String estadoGeneral) {
        this.estadoGeneral = estadoGeneral;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public List<Alerta> getAlertas() {
        return alertas;
    }

    public void setAlertas(List<Alerta> alertas) {
        this.alertas = alertas;
    }
}
