package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.example.demo.model.Medicion;
import com.example.demo.repository.MedicionRepository;
import com.example.demo.repository.MedicionRepository.Umbrales;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MedicionServiceTest {

    private MedicionRepository repository;
    private MedicionService service;

    @BeforeEach
    void prepararServicio() {
        repository = mock(MedicionRepository.class);
        service = new MedicionService(repository);
        when(repository.cargarUmbrales(1L)).thenReturn(new Umbrales(
                10.0, 18.0, 30.0, 35.0,
                40.0, 50.0, 70.0, 80.0));
        when(repository.guardar(any(Medicion.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void limitesOptimosSeClasificanComoOptimos() {
        Medicion minimo = service.registrar(1L, 19.0, 51.0);
        Medicion maximo = service.registrar(1L, 29.0, 69.0);

        assertEquals("OPTIMO", minimo.getEstadoGeneral());
        assertEquals("OPTIMO", maximo.getEstadoGeneral());
        assertEquals(0, minimo.getAlertas().size());
        assertEquals(0, maximo.getAlertas().size());
    }

    @Test
    void extremosPermitidosFueraDelOptimoGeneranAdvertencias() {
        Medicion medicion = service.registrar(1L, 10.0, 80.0);

        assertEquals("ADVERTENCIA", medicion.getEstadoTemperatura());
        assertEquals("ADVERTENCIA", medicion.getEstadoHumedad());
        assertEquals("ADVERTENCIA", medicion.getEstadoGeneral());
        assertEquals(2, medicion.getAlertas().size());
    }

    @Test
    void valoresFueraDeMaximosPermitidosGeneranAlertas() {
        Medicion sobreMaximo = service.registrar(1L, 36.0, 81.0);
        Medicion bajoMinimo = service.registrar(1L, 9.0, 39.0);

        assertEquals("ALERTA", sobreMaximo.getEstadoTemperatura());
        assertEquals("ALERTA", sobreMaximo.getEstadoHumedad());
        assertEquals("ALERTA", sobreMaximo.getEstadoGeneral());
        assertEquals(2, sobreMaximo.getAlertas().size());
        assertEquals("ALERTA", bajoMinimo.getEstadoTemperatura());
        assertEquals("ALERTA", bajoMinimo.getEstadoHumedad());
        assertEquals(2, bajoMinimo.getAlertas().size());
    }
}