package com.example.demo.model;

// Datos de una especie junto con sus límites ambientales y de riego.
public class Especie {

    private Long id;
    private String nombre;
    private String tipo;
    private String descripcion;
    private Double temperaturaOptimaMinima;
    private Double temperaturaOptimaMaxima;
    private Double temperaturaMinima;
    private Double temperaturaMaxima;
    private Double humedadOptimaMinima;
    private Double humedadOptimaMaxima;
    private Double humedadMinima;
    private Double humedadMaxima;
    private String faseCrecimiento;
    private Integer intervaloRiego;
    private Double volumenRiego;
    private String sustrato;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Double getTemperaturaOptimaMinima() {
        return temperaturaOptimaMinima;
    }

    public void setTemperaturaOptimaMinima(Double temperaturaOptimaMinima) {
        this.temperaturaOptimaMinima = temperaturaOptimaMinima;
    }

    public Double getTemperaturaOptimaMaxima() {
        return temperaturaOptimaMaxima;
    }

    public void setTemperaturaOptimaMaxima(Double temperaturaOptimaMaxima) {
        this.temperaturaOptimaMaxima = temperaturaOptimaMaxima;
    }

    public Double getTemperaturaMinima() {
        return temperaturaMinima;
    }

    public void setTemperaturaMinima(Double temperaturaMinima) {
        this.temperaturaMinima = temperaturaMinima;
    }

    public Double getTemperaturaMaxima() {
        return temperaturaMaxima;
    }

    public void setTemperaturaMaxima(Double temperaturaMaxima) {
        this.temperaturaMaxima = temperaturaMaxima;
    }

    public Double getHumedadOptimaMinima() {
        return humedadOptimaMinima;
    }

    public void setHumedadOptimaMinima(Double humedadOptimaMinima) {
        this.humedadOptimaMinima = humedadOptimaMinima;
    }

    public Double getHumedadOptimaMaxima() {
        return humedadOptimaMaxima;
    }

    public void setHumedadOptimaMaxima(Double humedadOptimaMaxima) {
        this.humedadOptimaMaxima = humedadOptimaMaxima;
    }

    public Double getHumedadMinima() {
        return humedadMinima;
    }

    public void setHumedadMinima(Double humedadMinima) {
        this.humedadMinima = humedadMinima;
    }

    public Double getHumedadMaxima() {
        return humedadMaxima;
    }

    public void setHumedadMaxima(Double humedadMaxima) {
        this.humedadMaxima = humedadMaxima;
    }

    public String getFaseCrecimiento() {
        return faseCrecimiento;
    }

    public void setFaseCrecimiento(String faseCrecimiento) {
        this.faseCrecimiento = faseCrecimiento;
    }

    public Integer getIntervaloRiego() {
        return intervaloRiego;
    }

    public void setIntervaloRiego(Integer intervaloRiego) {
        this.intervaloRiego = intervaloRiego;
    }

    public Double getVolumenRiego() {
        return volumenRiego;
    }

    public void setVolumenRiego(Double volumenRiego) {
        this.volumenRiego = volumenRiego;
    }

    public String getSustrato() {
        return sustrato;
    }

    public void setSustrato(String sustrato) {
        this.sustrato = sustrato;
    }
}
