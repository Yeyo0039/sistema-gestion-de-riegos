import { showSection } from "./section-loader.js";

// Conecta las pestañas del encabezado con los paneles visibles del dashboard.
function initializeNavigation() {
    const sectionPanel = document.querySelector("#section-panel");
    const sectionTabs = Array.from(document.querySelectorAll('[role="tab"][data-section]'));

    // Activa una sección y sincroniza los atributos de accesibilidad.
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
}

export { initializeNavigation };
