package com.example.demo.service;

import com.example.demo.model.Alerta;
import com.example.demo.model.Medicion;
import com.example.demo.repository.MedicionRepository;
import com.example.demo.repository.MedicionRepository.Umbrales;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

// Capa de negocio: transforma una lectura en estados, mensajes y alertas.
@Service
public class MedicionService {

    private static final String OPTIMO = "OPTIMO";
    private static final String ADVERTENCIA = "ADVERTENCIA";
    private static final String ALERTA = "ALERTA";

    private final MedicionRepository repository;

    public MedicionService(MedicionRepository repository) {
        this.repository = repository;
    }

    // Obtiene los umbrales del cultivo, clasifica cada variable y guarda el resultado.
    public Medicion registrar(Long cultivoId, Double temperatura, Double humedad) {
        Umbrales umbrales = repository.cargarUmbrales(cultivoId);
        Medicion medicion = new Medicion();
        medicion.setCultivoId(cultivoId);
        medicion.setTemperatura(temperatura);
        medicion.setHumedad(humedad);
        medicion.setFechaHora(java.time.LocalDateTime.now());

        medicion.setEstadoTemperatura(clasificar(temperatura,
                umbrales.temperaturaMinima(), umbrales.temperaturaOptimaMinima(),
                umbrales.temperaturaOptimaMaxima(), umbrales.temperaturaMaxima()));
        medicion.setMensajeTemperatura(crearMensaje("Temperatura", temperatura,
                umbrales.temperaturaMinima(), umbrales.temperaturaOptimaMinima(),
                umbrales.temperaturaOptimaMaxima(), umbrales.temperaturaMaxima(),
                medicion.getEstadoTemperatura()));

        medicion.setEstadoHumedad(clasificar(humedad,
                umbrales.humedadMinima(), umbrales.humedadOptimaMinima(),
                umbrales.humedadOptimaMaxima(), umbrales.humedadMaxima()));
        medicion.setMensajeHumedad(crearMensaje("Humedad", humedad,
                umbrales.humedadMinima(), umbrales.humedadOptimaMinima(),
                umbrales.humedadOptimaMaxima(), umbrales.humedadMaxima(),
                medicion.getEstadoHumedad()));

        medicion.setEstadoGeneral(estadoGeneral(medicion.getEstadoTemperatura(), medicion.getEstadoHumedad()));
        medicion.setMensaje(mensajeGeneral(medicion.getEstadoGeneral()));
        List<Alerta> alertas = new ArrayList<>();
        agregarAlerta(alertas, "temperatura", medicion.getEstadoTemperatura(), medicion.getMensajeTemperatura());
        agregarAlerta(alertas, "humedad", medicion.getEstadoHumedad(), medicion.getMensajeHumedad());
        medicion.setAlertas(alertas);

        return repository.guardar(medicion);
    }

    public List<Medicion> cargar() {
        return repository.cargar();
    }

    // Clasifica el valor en alerta, advertencia u óptimo según sus límites.
    private String clasificar(double valor, double minimo, double optimaMinima, 
            double optimaMaxima, double maximo) {
        if (valor < minimo || valor > maximo) {
            return ALERTA;
        }
        if (valor <= optimaMinima || valor >= optimaMaxima) {
            return ADVERTENCIA;
        }
        return OPTIMO;
    }

    // Genera el texto que verá el usuario para una variable concreta.
    private String crearMensaje(String variable, double valor, double minimo,
            double optimaMinima, double optimaMaxima, double maximo, String estado) {
        return switch (estado) {
            case OPTIMO -> variable + " dentro del rango óptimo (" + optimaMinima + " a " + optimaMaxima + ").";
            case ADVERTENCIA -> variable + " fuera del rango óptimo; rango recomendado: "
                    + optimaMinima + " a " + optimaMaxima + ". Valor: " + valor + ".";
            default -> variable + " fuera del límite permitido (" + minimo + " a " + maximo
                    + "). Valor: " + valor + ".";
        };
    }

    // El estado general conserva la condición más grave de las dos variables.
    private String estadoGeneral(String temperatura, String humedad) {
        if (ALERTA.equals(temperatura) || ALERTA.equals(humedad)) {
            return ALERTA;
        }
        if (ADVERTENCIA.equals(temperatura) || ADVERTENCIA.equals(humedad)) {
            return ADVERTENCIA;
        }
        return OPTIMO;
    }

    private String mensajeGeneral(String estado) {
        return switch (estado) {
            case OPTIMO -> "Temperatura y humedad están dentro de los rangos óptimos.";
            case ADVERTENCIA -> "Temperatura o humedad están fuera del rango óptimo.";
            default -> "Temperatura o humedad exceden los límites permitidos.";
        };
    }

    // Solo crea registros de alerta cuando el estado no es óptimo.
    private void agregarAlerta(List<Alerta> alertas, String variable, String estado, String mensaje) {
        if (OPTIMO.equals(estado)) {
            return;
        }
        Alerta alerta = new Alerta();
        alerta.setVariable(variable);
        alerta.setEstado(estado);
        alerta.setMensaje(mensaje);
        alertas.add(alerta);
    }
}
