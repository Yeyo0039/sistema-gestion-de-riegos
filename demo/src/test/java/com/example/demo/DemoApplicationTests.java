package com.example.demo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.demo.model.Cultivo;
import com.example.demo.model.Medicion;
import com.example.demo.service.CultivoController;
import com.example.demo.service.MedicionController;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import com.example.demo.repository.EspecieRepository;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.bind.annotation.RequestMethod;

// Pruebas de integración: levantan el contexto completo usando H2 en memoria.
@SpringBootTest(properties = {
		"spring.datasource.url=jdbc:h2:mem:application-test;DB_CLOSE_DELAY=-1",
		"spring.datasource.username=sa",
		"spring.datasource.password=",
		"spring.datasource.driver-class-name=org.h2.Driver"
})
class DemoApplicationTests {

	@Autowired
	private RequestMappingHandlerMapping handlerMapping;

	@Autowired
	private EspecieRepository especieRepository;

	@Autowired
	private CultivoController cultivoController;

	@Autowired
	private MedicionController medicionController;

	@Autowired
	private JdbcTemplate jdbc;

	@Test
	void contextLoads() {
	}

	@Test
	void cargarEspeciesEjecutaLaConsulta() {
		assertNotNull(especieRepository.cargar());
	}

	@Test
	void getEspeciesEstaMapeadoParaElSelector() {
		assertTrue(handlerMapping.getHandlerMethods().keySet().stream()
				.anyMatch(mapping -> mapping.getPatternValues().contains("/especies")
						&& mapping.getMethodsCondition().getMethods().contains(RequestMethod.GET)));
	}

	@Test
	@Transactional
	void cultivoGuardadoSeCargaParaElSelectorDeMediciones() {
		jdbc.update("INSERT INTO tipo (id, nombre) VALUES (?, ?)", 9001, "Tipo prueba");
		jdbc.update("INSERT INTO especie (id, nombre, tipo_id) VALUES (?, ?, ?)",
				9001, "Especie de prueba", 9001);

		Cultivo nuevo = new Cultivo();
		nuevo.setNombre("Cultivo de prueba");
		nuevo.setEspecieId(9001L);
		nuevo.setFechaInicio(LocalDate.of(2026, 11, 18));

		Cultivo guardado = cultivoController.crear(nuevo);

		assertNotNull(guardado.getId());
		assertTrue(cultivoController.cargar().stream()
				.anyMatch(cultivo -> cultivo.getId().equals(guardado.getId())
						&& cultivo.getNombre().equals("Cultivo de prueba")));
	}

	@Test
	void endpointsDeCultivoEstanMapeados() {
		Set<RequestMethod> metodos = new HashSet<>();
		handlerMapping.getHandlerMethods().forEach((mapping, handler) -> {
			if (mapping.getPatternValues().contains("/cultivos")) {
				metodos.addAll(mapping.getMethodsCondition().getMethods());
			}
		});

		assertTrue(metodos.containsAll(Set.of(RequestMethod.GET, RequestMethod.POST)));
	}

	@Test
	@Transactional
	void cincoMedicionesJuntasCreanEstadosMensajesYAlertas() {
		jdbc.update("INSERT INTO tipo (id, nombre) VALUES (?, ?)", 9002, "Tipo medicion");
		jdbc.update("INSERT INTO especie (id, nombre, tipo_id) VALUES (?, ?, ?)",
				9002, "Especie medicion", 9002);
		jdbc.update("""
				INSERT INTO temperatura (
					id, especie_id, optima_minima, optima_maxima, maximo_permitido, minimo_permitido
				) VALUES (?, ?, ?, ?, ?, ?)
				""", 9002, 9002, 18, 30, 35, 10);
		jdbc.update("""
				INSERT INTO humedad (
					id, especie_id, optima_minima, optima_maxima, maximo_permitido, minimo_permitido
				) VALUES (?, ?, ?, ?, ?, ?)
				""", 9002, 9002, 50, 70, 80, 40);

		Cultivo nuevoCultivo = new Cultivo();
		nuevoCultivo.setNombre("Cultivo para cinco lecturas");
		nuevoCultivo.setEspecieId(9002L);
		nuevoCultivo.setFechaInicio(LocalDate.of(2026, 11, 18));
		Cultivo cultivo = cultivoController.crear(nuevoCultivo);

		double[][] lecturas = {{24, 60}, {16, 45}, {32, 75}, {8, 35}, {36, 85}};
		String[] estadosEsperados = {"OPTIMO", "ADVERTENCIA", "ADVERTENCIA", "ALERTA", "ALERTA"};
		for (int index = 0; index < lecturas.length; index++) {
			Medicion entrada = new Medicion();
			entrada.setCultivoId(cultivo.getId());
			entrada.setTemperatura(lecturas[index][0]);
			entrada.setHumedad(lecturas[index][1]);
			Medicion guardada = medicionController.crear(entrada);
			assertEquals(estadosEsperados[index], guardada.getEstadoGeneral());
		}

		List<Medicion> recuperadas = medicionController.cargar().stream()
				.filter(medicion -> medicion.getCultivoId().equals(cultivo.getId())).toList();
		assertEquals(5, recuperadas.size());
		assertEquals(8, recuperadas.stream().mapToInt(medicion -> medicion.getAlertas().size()).sum());
		assertTrue(recuperadas.stream().allMatch(medicion -> medicion.getMensaje() != null));
	}

	@Test
	void endpointsDeMedicionEstanMapeados() {
		Set<RequestMethod> metodos = new HashSet<>();
		handlerMapping.getHandlerMethods().forEach((mapping, handler) -> {
			if (mapping.getPatternValues().contains("/mediciones")) {
				metodos.addAll(mapping.getMethodsCondition().getMethods());
			}
		});

		assertTrue(metodos.containsAll(Set.of(RequestMethod.GET, RequestMethod.POST)));
	}

	@Test
	void onlyHealthEndpointIsMappedForApiData() {
		Set<String> apiRoutes = new HashSet<>();
		handlerMapping.getHandlerMethods().keySet().forEach(mapping -> mapping.getPatternValues().stream()
				.filter(path -> path.startsWith("/api/"))
				.forEach(apiRoutes::add));

		assertEquals(Set.of("/api/health"), apiRoutes);
	}

}
