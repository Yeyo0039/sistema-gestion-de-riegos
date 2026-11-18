import { getJson, requestJson } from "../api.js";

// Formulario y listado de especies guardadas.
export function initializeSpeciesPanel() {
    const form = document.querySelector("#formulario-especie");
    const message = document.querySelector("#species-message");
    const output = document.querySelector("#resultado-especie");
    const list = document.querySelector("#species-list");
    const count = document.querySelector("#species-count");

    if (!(form instanceof HTMLFormElement) ||
        !(output instanceof HTMLElement)) {
        console.error("No se pudo inicializar el formulario de especies.");
        return;
    }

    if (message) {
        message.textContent = "Completa los datos de la especie.";
    }

    void cargarEspecies(list, count);
    window.addEventListener("especies-actualizadas", () => {
        void cargarEspecies(list, count);
    });

    limpiarErrores(form);

    form.addEventListener("input", (event) => {
        if (event.target instanceof HTMLInputElement ||
            event.target instanceof HTMLTextAreaElement ||
            event.target instanceof HTMLSelectElement) {
            marcarCampoValido(event.target);
        }
    });

    form.addEventListener("change", (event) => {
        if (event.target instanceof HTMLInputElement ||
            event.target instanceof HTMLTextAreaElement ||
            event.target instanceof HTMLSelectElement) {
            marcarCampoValido(event.target);
        }
    });

    form.addEventListener("submit", async (event) => {
        event.preventDefault();

        limpiarErrores(form);

        if (!form.reportValidity()) {
            marcarCamposHtmlInvalidos(form);
            mostrarMensaje(message, "Corrige los campos marcados.", true);
            return;
        }

        let payload;

        try {
            payload = obtenerDatosNormalizados(form);
            validarRangos(payload, form);

            output.textContent = JSON.stringify(payload, null, 2);
            output.hidden = false;

            const response = await crearEspecie(payload);

            mostrarMensaje(
                message,
                "Especie registrada correctamente.",
                false
            );
            window.dispatchEvent(new Event("especies-actualizadas"));

            console.log("Respuesta del servidor:", response);
        } catch (error) {
            marcarCamposDeRangoInvalidos(form, payload);
            mostrarMensaje(message, error.message, true);
            console.error("Error al registrar especie:", error);
        }
    });
}

// Normalizador de datos del formulario
function obtenerDatosNormalizados(form) {
    const payload = {};

    for (const [name, valor] of new FormData(form).entries()) {
        const campo = form.elements.namedItem(name);

        if (campo instanceof HTMLInputElement && campo.type === "number") {
            const texto = String(valor).trim();

            if (texto === "") {
                payload[name] = null;
                continue;
            }

            const numero = Number(texto);

            if (!Number.isFinite(numero)) {
                throw new Error(`El campo "${campo.labels?.[0]?.textContent || name}" debe ser numérico.`);
            }

            payload[name] = numero;
        } else {
            payload[name] = String(valor).trim();
        }
    }

    return payload;
}

// Validar rangos de temperatura y humedad
function validarRangos(payload, form) {
    const temperatura = {
        minima: payload.temperaturaMinima,
        optimaMinima: payload.temperaturaOptimaMinima,
        optimaMaxima: payload.temperaturaOptimaMaxima,
        maxima: payload.temperaturaMaxima
    };

    const humedad = {
        minima: payload.humedadMinima,
        optimaMinima: payload.humedadOptimaMinima,
        optimaMaxima: payload.humedadOptimaMaxima,
        maxima: payload.humedadMaxima
    };

    validarGrupo("Temperatura", temperatura, form);
    validarGrupo("Humedad", humedad, form);
}


function validarGrupo(nombre, rango, form) {
    const valores = [rango.minima, rango.optimaMinima, rango.optimaMaxima, rango.maxima];

    if (valores.some(valor => typeof valor !== "number" || !Number.isFinite(valor))) {
        throw new Error(`${nombre}: todos los rangos deben ser numéricos.`);
    }

    if (rango.minima > rango.optimaMinima ||
        rango.optimaMinima > rango.optimaMaxima ||
        rango.optimaMaxima > rango.maxima) {
        for (const valorNombre of ["minima", "optimaMinima", "optimaMaxima", "maxima"]) {
            const campoNombre = (nombre === "Humedad" ? "humedad" : "temperatura")
                + valorNombre.charAt(0).toUpperCase()
                + valorNombre.slice(1);
            marcarCampoInvalido(form.elements.namedItem(campoNombre));
        }

        throw new Error(
            `${nombre}: mínimo permitido <= óptimo mínimo <= óptimo máximo <= máximo permitido.`
        );
    }

    if (nombre === "Humedad" && (rango.minima < 0 || rango.maxima > 100)) {
        throw new Error("La humedad debe estar comprendida entre 0% y 100%.");
    }

    const prefijo = nombre === "Humedad" ? "humedad" : "temperatura";
    for (const [propiedad, valor] of Object.entries(rango)) {
        const nombreCampo = prefijo + propiedad.charAt(0).toUpperCase() + propiedad.slice(1);
        validarConRestriccionesHtml(form, nombreCampo, valor, nombre);
    }
}

// Restricciones de formulario con html
function validarConRestriccionesHtml(form, nombre, valor, etiqueta) {
    const campo = form.elements.namedItem(nombre);

    if (!(campo instanceof HTMLInputElement)) {
        return;
    }

    if (campo.min !== "" && valor < Number(campo.min)) {
        throw new Error(
            `${etiqueta}: el valor mínimo permitido es ${campo.min}.`
        );
    }

    if (campo.max !== "" && valor > Number(campo.max)) {
        throw new Error(
            `${etiqueta}: el valor máximo permitido es ${campo.max}.`
        );
    }
}

function limpiarErrores(form) {
    for (const campo of form.querySelectorAll("input, select, textarea")) {
        marcarCampoValido(campo);
    }
}

function marcarCampoValido(campo) {
    campo.classList.remove("is-invalid");
    campo.removeAttribute("aria-invalid");
}

function marcarCampoInvalido(campo) {
    if (campo instanceof HTMLInputElement ||
        campo instanceof HTMLSelectElement ||
        campo instanceof HTMLTextAreaElement) {
        campo.classList.add("is-invalid");
        campo.setAttribute("aria-invalid", "true");
    }
}

function marcarCamposHtmlInvalidos(form) {
    for (const campo of form.querySelectorAll("input, select, textarea")) {
        if (!campo.checkValidity()) {
            marcarCampoInvalido(campo);
        }
    }
}

function marcarCamposDeRangoInvalidos(form, payload) {
    if (!payload) {
        return;
    }

    for (const [nombre, valor] of Object.entries(payload)) {
        const campo = form.elements.namedItem(nombre);

        if (campo instanceof HTMLInputElement &&
            (campo.type === "number" && (campo.min !== "" && valor < Number(campo.min) ||
                campo.max !== "" && valor > Number(campo.max)))) {
            marcarCampoInvalido(campo);
        }
    }
}

function mostrarMensaje(message, texto, esError) {
    if (!message) {
        return;
    }

    message.textContent = texto;
    message.classList.toggle("form-error", esError);
}

export async function crearEspecie(json) {
    try {
        const res = await requestJson("/especies", {
            method: "POST",
            data: json
        });
        console.log(res);
        return res;
    } catch (error) {
        const mensaje = error instanceof Error
            ? error.message
            : "No fue posible registrar la especie.";

        throw new Error(mensaje);
    }
}

// Renderiza cada especie y actualiza el contador con el mismo conjunto.
async function cargarEspecies(list, count) {
    if (!(list instanceof HTMLElement) || !(count instanceof HTMLElement)) {
        return;
    }

    try {
        const especies = await getJson("/especies");
        if (!Array.isArray(especies)) {
            throw new Error("La respuesta de especies no tiene el formato esperado.");
        }

        const tarjetas = especies
            .filter((especie) => especie?.id != null && typeof especie.nombre === "string")
            .map((especie) => {
                const tarjeta = document.createElement("article");
                tarjeta.className = "section-item";
                const nombre = document.createElement("h4");
                nombre.textContent = especie.nombre;
                const id = document.createElement("p");
                id.textContent = `Registro #${especie.id}`;
                tarjeta.append(nombre, id);
                return tarjeta;
            });

        count.textContent = String(tarjetas.length);
        list.replaceChildren(...tarjetas);
        if (tarjetas.length === 0) {
            list.append(crearMensajeLista("No hay especies registradas."));
        }
    } catch (error) {
        count.textContent = "0";
        list.replaceChildren(crearMensajeLista(
            error instanceof Error ? error.message : "No se pudieron cargar las especies."
        ));

    }

function crearMensajeLista(texto) {
    const mensaje = document.createElement("p");
    mensaje.textContent = texto;
    return mensaje;
}
}