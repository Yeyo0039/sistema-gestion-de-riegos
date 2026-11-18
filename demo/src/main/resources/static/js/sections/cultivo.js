import { getJson, requestJson } from "../api.js";

// Formulario, selector de especies y listado de cultivos.
export function initializeCropsPanel() {
    const form = document.querySelector("#formulario-cultivo");
    const message = document.querySelector("#crop-message");
    const speciesSelect = document.querySelector("#crop-species");
    const output = document.querySelector("#resultado-cultivo");
    const list = document.querySelector("#crops-list");
    const count = document.querySelector("#crop-count");

    if (!(form instanceof HTMLFormElement) ||
        !(speciesSelect instanceof HTMLSelectElement) ||
        !(output instanceof HTMLElement) ||
        !(list instanceof HTMLElement) ||
        !(count instanceof HTMLElement)) {
        console.error("No se pudo inicializar el formulario de cultivo.");
        return;
    }

    if (message) {
        message.textContent = "Completa los datos del cultivo.";
    }

    void cargarCultivos(list, count);
    window.addEventListener("cultivos-actualizados", () => {
        void cargarCultivos(list, count);
    });
    window.addEventListener("especies-actualizadas", () => {
        void consultarEspecies(speciesSelect, message);
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
        datos.especieId = Number(datos.especieId);
        output.textContent = JSON.stringify(datos, null, 2);
        output.hidden = false;

        const boton = form.querySelector("button[type='submit']");
        if (boton instanceof HTMLButtonElement) {
            boton.disabled = true;
        }

        try {
            const cultivo = await requestJson("/cultivos", {
                method: "POST",
                data: datos
            });
            output.textContent = JSON.stringify(cultivo, null, 2);
            mostrarMensaje(message, "Cultivo registrado correctamente.", false);
            window.dispatchEvent(new Event("cultivos-actualizados"));
        } catch (error) {
            mostrarMensaje(
                message,
                error instanceof Error ? error.message : "No se pudo registrar el cultivo.",
                true
            );
        } finally {
            if (boton instanceof HTMLButtonElement) {
                boton.disabled = false;
            }
        }
    });

    void consultarEspecies(speciesSelect, message);
}

// Renderiza la lista de cultivos y sincroniza su contador.
async function cargarCultivos(list, count) {
    try {
        const [cultivos, especies] = await Promise.all([
            getJson("/cultivos"),
            getJson("/especies")
        ]);
        if (!Array.isArray(cultivos) || !Array.isArray(especies)) {
            throw new Error("La respuesta de cultivos no tiene el formato esperado.");
        }

        const especiesPorId = new Map(especies.map((especie) => [Number(especie.id), especie.nombre]));
        const tarjetas = cultivos.map((cultivo) => {
            const tarjeta = document.createElement("article");
            tarjeta.className = "cultivo-card";
            const encabezado = document.createElement("div");
            encabezado.className = "cultivo-header";
            const datos = document.createElement("div");
            const nombre = document.createElement("h4");
            nombre.className = "nombre-cultivo";
            nombre.textContent = cultivo.nombre;
            const especie = document.createElement("span");
            especie.className = "especie-cultivo";
            especie.textContent = especiesPorId.get(Number(cultivo.especieId)) || "Especie no disponible";
            datos.append(nombre, especie);
            encabezado.append(datos);

            const fecha = document.createElement("p");
            fecha.className = "fecha-cultivo";
            fecha.textContent = cultivo.fechaInicio
                ? `Fecha de inicio: ${new Date(`${cultivo.fechaInicio}T00:00:00`).toLocaleDateString("es-MX")}`
                : "Fecha de inicio no disponible";
            tarjeta.append(encabezado, fecha);
            return tarjeta;
        });

        count.textContent = String(tarjetas.length);
        list.replaceChildren(...tarjetas);
        if (tarjetas.length === 0) {
            list.append(crearMensajeLista("No hay cultivos registrados."));
        }
    } catch (error) {
        count.textContent = "0";
        list.replaceChildren(crearMensajeLista(
            error instanceof Error ? error.message : "No se pudieron cargar los cultivos."
        ));
    }
}

function crearMensajeLista(texto) {
    const mensaje = document.createElement("p");
    mensaje.textContent = texto;
    return mensaje;
}

async function consultarEspecies(select, message) {
    select.disabled = true;
    mostrarMensaje(message, "Cargando especies...", false);

    try {
        const especies = await getJson("/especies");
        if (!Array.isArray(especies)) {
            throw new Error("La respuesta de especies no tiene el formato esperado.");
        }

        const opciones = especies
            .filter((especie) => especie?.id != null && typeof especie.nombre === "string")
            .map((especie) => new Option(especie.nombre, String(especie.id)));

        select.replaceChildren(new Option(
            opciones.length ? "Selecciona una especie" : "No hay especies registradas",
            ""
        ));
        select.options[0].disabled = true;
        select.options[0].selected = true;
        select.append(...opciones);

        mostrarMensaje(
            message,
            opciones.length ? "Completa los datos del cultivo." : "Registra una especie antes de crear un cultivo.",
            false
        );
    } catch (error) {
        select.replaceChildren(new Option("No se pudieron cargar las especies", ""));
        select.options[0].disabled = true;
        select.options[0].selected = true;
        mostrarMensaje(
            message,
            error instanceof Error ? error.message : "No se pudieron cargar las especies.",
            true
        );
    } finally {
        select.disabled = false;
    }
}

function limpiarErrores(form) {
    for (const campo of form.querySelectorAll("input, select, textarea")) {
        marcarCampoValido(campo);
    }
}

function marcarCamposInvalidos(form) {
    for (const campo of form.querySelectorAll("input, select, textarea")) {
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
