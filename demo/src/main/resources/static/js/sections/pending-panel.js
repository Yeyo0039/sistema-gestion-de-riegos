// Comportamiento temporal para formularios que todavía no tienen endpoint.
export function preparePendingPanel(formSelector, messageSelector) {
    const form = document.querySelector(formSelector);
    const message = document.querySelector(messageSelector);

    if (message) {
        message.textContent = "La conexión de este panel se implementará después.";
    }

    if (!form) {
        return;
    }

    form.addEventListener("submit", (event) => {
        event.preventDefault();
        if (message) {
            message.textContent = "Este panel todavía no guarda datos.";
            message.classList.remove("form-error");
        }
    });
}
