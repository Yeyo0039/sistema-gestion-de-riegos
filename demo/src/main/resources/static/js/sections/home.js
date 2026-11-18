import { getJson } from "../api.js";

// Resumen del Home con cultivos, especies, mediciones y métricas reales.
export function initializeHomePanel() {
    const cropsList = document.querySelector("#home-crops-list");
    const alertsList = document.querySelector("#home-alerts-list");
    const message = document.querySelector("#home-message");

    if (!(cropsList instanceof HTMLElement) || !(alertsList instanceof HTMLElement)) {
        return;
    }

    void cargarResumen(cropsList, alertsList, message);
    window.addEventListener("cultivos-actualizados", () => {
        void cargarResumen(cropsList, alertsList, message);
    });
    window.addEventListener("mediciones-actualizadas", () => {
        void cargarResumen(cropsList, alertsList, message);
    });
}

async function cargarResumen(cropsList, alertsList, message) {
    if (message) {
        message.textContent = "Cargando datos guardados...";
        message.classList.remove("form-error");
    }

    try {
        const [cultivos, especies, mediciones] = await Promise.all([
            getJson("/cultivos"),
            getJson("/especies"),
            getJson("/mediciones")
        ]);

        if (!Array.isArray(cultivos) || !Array.isArray(especies) || !Array.isArray(mediciones)) {
            throw new Error("El servidor devolvió datos con un formato inesperado.");
        }

        const especiesPorId = new Map(especies.map((especie) => [Number(especie.id), especie.nombre]));
        const ultimaMedicionPorCultivo = new Map();
        for (const medicion of mediciones) {
            const cultivoId = Number(medicion.cultivoId);
            if (!ultimaMedicionPorCultivo.has(cultivoId)) {
                ultimaMedicionPorCultivo.set(cultivoId, medicion);
            }
        }

        cropsList.replaceChildren(...cultivos.map((cultivo) => crearTarjetaCultivo(
            cultivo,
            especiesPorId.get(Number(cultivo.especieId)),
            ultimaMedicionPorCultivo.get(Number(cultivo.id))
        )));
        document.querySelector("#home-crop-count").textContent = String(cultivos.length);
        document.querySelector("#home-species-count").textContent = String(especies.length);

        const cultivosPorId = new Map(cultivos.map((cultivo) => [Number(cultivo.id), cultivo.nombre]));
        const alertas = mediciones.flatMap((medicion) =>
            (medicion.alertas ?? []).map((alerta) => ({
                ...alerta,
                cultivoNombre: medicion.cultivoNombre || cultivosPorId.get(Number(medicion.cultivoId)),
                fechaHora: medicion.fechaHora
            }))
        ).slice(0, 4);
        alertsList.replaceChildren(...alertas.map(crearFilaAlerta));
        if (alertas.length === 0) {
            alertsList.append(crearTexto("Sin alertas registradas."));
        }

        if (message) {
            message.textContent = `Datos actualizados: ${cultivos.length} cultivos, ${especies.length} especies y ${mediciones.length} mediciones.`;
        }
    } catch (error) {
        if (message) {
            message.textContent = error instanceof Error ? error.message : "No se pudo cargar el resumen.";
            message.classList.add("form-error");
        }
    }
}

function crearTarjetaCultivo(cultivo, especieNombre, medicion) {
    const tarjeta = document.createElement("article");
    tarjeta.className = "cultivo-card";

    const encabezado = document.createElement("div");
    encabezado.className = "cultivo-header";
    const identidad = document.createElement("div");
    const nombre = document.createElement("h4");
    nombre.className = "nombre-cultivo";
    nombre.textContent = cultivo.nombre;
    const especie = document.createElement("span");
    especie.className = "especie-cultivo";
    especie.textContent = especieNombre || "Especie no disponible";
    identidad.append(nombre, especie);

    const estado = document.createElement("span");
    estado.className = `estado-badge ${claseEstado(medicion?.estadoGeneral)}`;
    estado.textContent = etiquetaEstado(medicion?.estadoGeneral);
    encabezado.append(identidad, estado);

    const fecha = document.createElement("p");
    fecha.className = "fecha-cultivo";
    fecha.textContent = `Fecha de inicio: ${formatearFecha(cultivo.fechaInicio)}`;

    const lecturas = document.createElement("div");
    lecturas.className = "mediciones-grid";
    lecturas.append(
        crearLectura("Temperatura:", medicion?.temperatura, "°C"),
        crearLectura("Humedad:", medicion?.humedad, "%")
    );

    tarjeta.append(encabezado, fecha, lecturas);
    return tarjeta;
}

function crearLectura(etiqueta, valor, unidad) {
    const lectura = document.createElement("div");
    lectura.className = "medicion-item";
    const titulo = document.createElement("span");
    titulo.textContent = etiqueta;
    const resultado = document.createElement("span");
    resultado.className = "medicion-val";
    resultado.textContent = Number.isFinite(Number(valor)) && valor !== null
        ? `${Number(valor).toFixed(1)} ${unidad}`
        : "Sin datos";
    lectura.append(titulo, " ", resultado);
    return lectura;
}

function crearFilaAlerta(alerta) {
    const fila = document.createElement("div");
    fila.className = "alerta-item";
    const texto = document.createElement("span");
    const variable = alerta.variable === "temperatura" ? "Temperatura" : "Humedad";
    texto.textContent = `${alerta.cultivoNombre || "Cultivo"} · ${variable}: ${alerta.mensaje}`;
    const estado = document.createElement("span");
    estado.className = `estado-badge ${claseEstado(alerta.estado)}`;
    estado.textContent = etiquetaEstado(alerta.estado);
    fila.append(texto, estado);
    return fila;
}

function claseEstado(estado) {
    if (estado === "OPTIMO") {
        return "badge-optimo";
    }
    if (estado === "ADVERTENCIA") {
        return "badge-atencion";
    }
    return estado ? "badge-critico" : "estado-conectando";
}

function etiquetaEstado(estado) {
    if (estado === "OPTIMO") {
        return "ÓPTIMO";
    }
    if (estado === "ADVERTENCIA") {
        return "ATENCIÓN";
    }
    return estado || "SIN MEDICIONES";
}

function formatearFecha(fecha) {
    if (!fecha) {
        return "Sin fecha";
    }
    return new Date(`${fecha}T00:00:00`).toLocaleDateString("es-MX");
}

function crearTexto(texto) {
    const elemento = document.createElement("span");
    elemento.textContent = texto;
    return elemento;
}