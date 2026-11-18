import { getJson, requestJson } from "../api.js";

// Formulario de lecturas, lista de mediciones y contador.
export function initializeMeasurementsPanel() {
    const form = document.querySelector("#formulario-medicion");
    const select = document.querySelector("#measurement-crop");
    const message = document.querySelector("#measurement-message");
    const list = document.querySelector("#measurements-list");
    const listMessage = document.querySelector("#measurement-list-message");

    if (!(form instanceof HTMLFormElement) ||
        !(select instanceof HTMLSelectElement) ||
        !(list instanceof HTMLElement)) {
        console.error("No se pudo inicializar el formulario de mediciones.");
        return;
    }

    void consultarCultivos(select, message);
    void consultarMediciones(list, listMessage);
    window.addEventListener("cultivos-actualizados", () => {
        void consultarCultivos(select, message);
    });
    window.addEventListener("mediciones-actualizadas", () => {
        void consultarMediciones(list, listMessage);
    });

    form.addEventListener("input", (event) => marcarCampoValido(event.target));
    form.addEventListener("change", (event) => marcarCampoValido(event.target));
    form.addEventListener("submit", async (event) => {
        event.preventDefault();
        limpiarErrores(form);
        if (!form.reportValidity()) {
            marcarCamposInvalidos(form);
            mostrarMensaje(message, "Corrige los campos marcados.", true);
            return;
        }

        const datos = Object.fromEntries(new FormData(form).entries());
        datos.cultivoId = Number(datos.cultivoId);
        datos.temperatura = Number(datos.temperatura);
        datos.humedad = Number(datos.humedad);
        const boton = form.querySelector("button[type='submit']");
        if (boton instanceof HTMLButtonElement) {
            boton.disabled = true;
        }

        try {
            const medicion = await requestJson("/mediciones", { method: "POST", data: datos });
            mostrarMensaje(message, "Medición de temperatura y humedad registrada.", false);
            window.dispatchEvent(new Event("mediciones-actualizadas"));
            return medicion;
        } catch (error) {
            mostrarMensaje(message,
                error instanceof Error ? error.message : "No se pudo guardar la medición.", true);
        } finally {
            if (boton instanceof HTMLButtonElement) {
                boton.disabled = false;
            }
        }
    });
}

async function consultarCultivos(select, message) {
    select.disabled = true;
    mostrarMensaje(message, "Cargando cultivos...", false);

    try {
        const cultivos = await getJson("/cultivos");
        if (!Array.isArray(cultivos)) {
            throw new Error("La respuesta de cultivos no tiene el formato esperado.");
        }

        const opciones = cultivos
            .filter((cultivo) => cultivo?.id != null && typeof cultivo.nombre === "string")
            .map((cultivo) => new Option(cultivo.nombre, String(cultivo.id)));

        select.replaceChildren(new Option(
            opciones.length ? "Selecciona un cultivo" : "No hay cultivos registrados",
            ""
        ));
        select.options[0].disabled = true;
        select.options[0].selected = true;
        select.append(...opciones);

        mostrarMensaje(
            message,
            opciones.length ? "Selecciona el cultivo para la medición." : "Registra un cultivo para poder agregar mediciones.",
            false
        );
    } catch (error) {
        select.replaceChildren(new Option("No se pudieron cargar los cultivos", ""));
        select.options[0].disabled = true;
        select.options[0].selected = true;
        mostrarMensaje(
            message,
            error instanceof Error ? error.message : "No se pudieron cargar los cultivos.",
            true
        );
    } finally {
        select.disabled = false;
    }
}

async function consultarMediciones(list, message) {
    if (message) {
        message.textContent = "Cargando mediciones...";
    }

    try {
        const mediciones = await getJson("/mediciones");
        if (!Array.isArray(mediciones)) {
            throw new Error("La respuesta de mediciones no tiene el formato esperado.");
        }

        list.replaceChildren(...mediciones.map(crearTarjetaMedicion));
        // El contador refleja las mismas tarjetas renderizadas en la lista.
        const count = document.querySelector("#measurement-count");
        if (count) {
            count.textContent = String(mediciones.length);
        }
        if (message) {
            message.textContent = mediciones.length ? "Mediciones cargadas." : "Aún no hay mediciones registradas.";
            message.classList.remove("form-error");
        }
    } catch (error) {
        list.replaceChildren();
        if (message) {
            message.textContent = error instanceof Error ? error.message : "No se pudieron cargar las mediciones.";
            message.classList.add("form-error");
        }
    }
}

function crearTarjetaMedicion(medicion) {
    const tarjeta = document.createElement("article");
    tarjeta.className = "measurement-record-card";

    const encabezado = document.createElement("div");
    encabezado.className = "measurement-record-heading";
    const identificacion = document.createElement("div");
    const cultivo = document.createElement("h4");
    cultivo.className = "measurement-record-name";
    cultivo.textContent = medicion.cultivoNombre || "Cultivo";
    const fecha = document.createElement("p");
    fecha.className = "form-note measurement-record-date";
    fecha.textContent = medicion.fechaHora
        ? new Date(medicion.fechaHora).toLocaleString("es-MX")
        : "Fecha no disponible";
    identificacion.append(cultivo, fecha);

    const estado = document.createElement("span");
    estado.className = `estado-badge ${claseEstadoMedicion(medicion.estadoGeneral)}`;
    estado.textContent = etiquetaEstadoMedicion(medicion.estadoGeneral);
    encabezado.append(identificacion, estado);

    const lecturas = document.createElement("div");
    lecturas.className = "mediciones-grid measurement-record-values";
    lecturas.append(
        crearLecturaMedicion("Temperatura", medicion.temperatura, "°C", medicion.estadoTemperatura),
        crearLecturaMedicion("Humedad", medicion.humedad, "%", medicion.estadoHumedad)
    );
    tarjeta.append(encabezado, lecturas);
    return tarjeta;
}

function crearLecturaMedicion(nombre, valor, unidad, estado) {
    const lectura = document.createElement("div");
    lectura.className = "medicion-item measurement-reading-item";
    const etiqueta = document.createElement("span");
    etiqueta.textContent = `${nombre}:`;
    const dato = document.createElement("span");
    dato.className = "medicion-val";
    dato.textContent = Number.isFinite(Number(valor)) && valor !== null
        ? `${Number(valor).toFixed(1)} ${unidad}`
        : "Sin datos";
    lectura.append(etiqueta, dato);
    return lectura;
}

function claseEstadoMedicion(estado) {
    if (estado === "OPTIMO") {
        return "badge-optimo";
    }
    if (estado === "ADVERTENCIA") {
        return "badge-atencion";
    }
    return estado === "ALERTA" ? "badge-critico" : "estado-conectando";
}

function etiquetaEstadoMedicion(estado) {
    if (estado === "OPTIMO") {
        return "ÓPTIMO";
    }
    if (estado === "ADVERTENCIA") {
        return "ATENCIÓN";
    }
    return estado || "SIN DATOS";
}

function limpiarErrores(form) {
    for (const campo of form.querySelectorAll("input, select")) {
        marcarCampoValido(campo);
    }
}

function marcarCamposInvalidos(form) {
    for (const campo of form.querySelectorAll("input, select")) {
        if (!campo.checkValidity()) {
            campo.classList.add("is-invalid");
            campo.setAttribute("aria-invalid", "true");
        }
    }
}

function marcarCampoValido(campo) {
    if (campo instanceof HTMLInputElement || campo instanceof HTMLSelectElement) {
        campo.classList.remove("is-invalid");
        campo.removeAttribute("aria-invalid");
    }
}

function mostrarMensaje(message, texto, esError) {
    if (!message) {
        return;
    }

    message.textContent = texto;
    message.classList.toggle("form-error", esError);
}
