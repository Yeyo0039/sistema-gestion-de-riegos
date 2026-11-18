import { getJson } from "./api.js";

// Actualiza el indicador visual según el resultado de /api/health.
export async function checkBackendStatus() {
    const backendStatus = document.querySelector("#backend-status");
    if (!backendStatus) {
        return;
    }

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
