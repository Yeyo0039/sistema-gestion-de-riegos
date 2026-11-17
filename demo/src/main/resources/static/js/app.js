import { getJson, postJson } from "./api.js";
import { showSection } from "./section-loader.js";

const backendStatus = document.querySelector("#backend-status");
const sectionPanel = document.querySelector("#section-panel");
const sectionTabs = Array.from(document.querySelectorAll('[role="tab"][data-section]'));
let species = [];
let crops = [];

async function checkBackend() {
    try {
        await getJson("/api/health");
        backendStatus.textContent = "Backend: conectado";
        backendStatus.className = "estado-badge estado-conectado";
    } catch (error) {
        console.error("No se pudo conectar con la API:", error);
        backendStatus.textContent = "Backend: desconectado";
        backendStatus.className = "estado-badge estado-desconectado";
    }
}

function showMessage(selector, message, isError = false) {
    const element = document.querySelector(selector);
    if (element) {
        element.textContent = message;
        element.classList.toggle("form-error", isError);
    }
}

function createTextElement(tagName, className, text) {
    const element = document.createElement(tagName);
    if (className) {
        element.className = className;
    }
    element.textContent = text;
    return element;
}

function updateSelect(select, items, placeholder, idKey, labelKey) {
    if (!select) {
        return;
    }

    select.replaceChildren(new Option(placeholder, ""));
    items.forEach((item) => {
        select.add(new Option(item[labelKey], item[idKey]));
    });
}

function renderSpecies(items) {
    const list = document.querySelector("#species-list");
    if (!list) {
        return;
    }

    list.replaceChildren();
    items.forEach((item) => {
        const article = createTextElement("article", "section-item", "");
        const heading = createTextElement("div", "item-heading", "");
        heading.append(
            createTextElement("h4", "", item.nombre),
            createTextElement("span", "estado-badge badge-optimo", item.tipo)
        );
        article.append(heading, createTextElement("p", "", item.descripcion));
        list.append(article);
    });
}

function renderCrops(items) {
    const list = document.querySelector("#crops-list");
    if (!list) {
        return;
    }

    list.replaceChildren();
    items.forEach((item) => {
        const article = createTextElement("article", "cultivo-card", "");
        const heading = createTextElement("div", "cultivo-header", "");
        const details = createTextElement("div", "", "");
        details.append(
            createTextElement("h4", "nombre-cultivo", item.nombre),
            createTextElement("span", "especie-cultivo", item.especieNombre)
        );
        heading.append(details);
        article.append(
            heading,
            createTextElement("p", "fecha-cultivo", `Fecha de inicio: ${item.fechaInicio}`)
        );
        list.append(article);
    });
}

function renderMeasurements(items) {
    const tableBody = document.querySelector("#measurements-list");
    if (!tableBody) {
        return;
    }

    tableBody.replaceChildren();
    items.forEach((item) => {
        const row = document.createElement("tr");
        [
            item.cultivoNombre,
            item.tipo,
            `${item.valor}${item.tipo === "temperatura" ? " °C" : " %"}`,
            new Date(item.fecha).toLocaleString()
        ].forEach((value) => row.append(createTextElement("td", "", String(value))));
        tableBody.append(row);
    });
}

async function loadData() {
    try {
        [species, crops] = await Promise.all([
            getJson("/api/species"),
            getJson("/api/crops")
        ]);
        const measurements = await getJson("/api/measurements");
        renderSpecies(species);
        renderCrops(crops);
        renderMeasurements(measurements);
        updateSelect(document.querySelector("#crop-species"), species, "Selecciona una especie", "id", "nombre");
        updateSelect(document.querySelector("#measurement-crop"), crops, "Selecciona un cultivo", "id", "nombre");
    } catch (error) {
        console.error("No se pudieron cargar los datos:", error);
        showMessage("#species-message", error.message, true);
        showMessage("#crop-message", error.message, true);
        showMessage("#measurement-message", error.message, true);
    }
}

function numericFields(formData, names) {
    const values = Object.fromEntries(formData.entries());
    names.forEach((name) => {
        values[name] = Number(values[name]);
    });
    return values;
}

function bindJsonForm(formSelector, endpoint, messageSelector, numberFields, afterSave) {
    const form = document.querySelector(formSelector);
    if (!form) {
        return;
    }

    form.addEventListener("submit", async (event) => {
        event.preventDefault();
        const submitButton = form.querySelector('[type="submit"]');
        submitButton.disabled = true;
        showMessage(messageSelector, "Guardando...");

        try {
            const body = numericFields(new FormData(form), numberFields);
            await postJson(endpoint, body);
            form.reset();
            showMessage(messageSelector, "Registro guardado correctamente.");
            await afterSave();
        } catch (error) {
            console.error(`No se pudo guardar ${endpoint}:`, error);
            showMessage(messageSelector, error.message, true);
        } finally {
            submitButton.disabled = false;
        }
    });
}

bindJsonForm("#formulario-especie", "/api/species", "#species-message", [
    "temperaturaOptimaMinima", "temperaturaOptimaMaxima", "temperaturaMinima",
    "temperaturaMaxima", "humedadOptimaMinima", "humedadOptimaMaxima",
    "humedadMinima", "humedadMaxima", "intervaloRiego", "volumenRiego"
], loadData);
bindJsonForm("#formulario-cultivo", "/api/crops", "#crop-message", ["especieId"], loadData);
bindJsonForm("#formulario-medicion", "/api/measurements", "#measurement-message", [
    "cultivoId", "valor"
], loadData);

if (backendStatus) {
    checkBackend();
}
loadData();

function activateSection(sectionName, moveFocus = false) {
    if (!sectionPanel || !showSection(sectionPanel, sectionName)) {
        return false;
    }

    let activeTab = null;
    sectionTabs.forEach((tab) => {
        const isCurrentTab = tab.dataset.section === sectionName;
        tab.classList.toggle("active", isCurrentTab);
        tab.setAttribute("aria-selected", String(isCurrentTab));
        tab.tabIndex = isCurrentTab ? 0 : -1;

        if (isCurrentTab) {
            activeTab = tab;
        }
    });

    if (moveFocus && activeTab) {
        activeTab.focus();
    }

    return true;
}

document.addEventListener("click", (event) => {
    const target = event.target;
    const button = target instanceof Element ? target.closest("[data-section]") : null;

    if (!button) {
        return;
    }

    const isTabClick = button.getAttribute("role") === "tab";
    activateSection(button.dataset.section, !isTabClick);
});

activateSection("home");

document.addEventListener("keydown", (event) => {
    const target = event.target;
    const currentTab = target instanceof Element
        ? target.closest('[role="tab"][data-section]')
        : null;

    if (!currentTab) {
        return;
    }

    const currentIndex = sectionTabs.indexOf(currentTab);
    let nextIndex = currentIndex;

    if (event.key === "ArrowRight") {
        nextIndex = (currentIndex + 1) % sectionTabs.length;
    } else if (event.key === "ArrowLeft") {
        nextIndex = (currentIndex - 1 + sectionTabs.length) % sectionTabs.length;
    } else if (event.key === "Home") {
        nextIndex = 0;
    } else if (event.key === "End") {
        nextIndex = sectionTabs.length - 1;
    } else {
        return;
    }

    event.preventDefault();
    activateSection(sectionTabs[nextIndex].dataset.section, true);
});
