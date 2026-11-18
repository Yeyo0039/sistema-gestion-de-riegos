
// Cliente HTTP común para todos los módulos del frontend.
export async function requestJson(url, {
    method = 'GET',
    data,
    headers = {},
    ...options
} = {}) {
    // Solo se serializa un cuerpo cuando la operación recibió datos.
    const hasBody = data !== undefined && data !== null;

    const response = await fetch(url, {
        ...options,
        method,
        headers: {
            ...(hasBody ? { 'Content-Type': 'application/json' } : {}),
            ...headers
        },
        ...(hasBody ? { body: JSON.stringify(data) } : {})
    });

    // La respuesta puede ser JSON o texto según el endpoint.
    const contentType = response.headers.get('content-type') || '';

    const result = contentType.includes('application/json')
        ? await response.json()
        : await response.text();

    // Convierte los errores HTTP en excepciones que cada panel puede mostrar.
    if (!response.ok) {
        throw new Error(
            result?.message || result?.error || result || `HTTP ${response.status}`
        );
    }

    return result;
}


export function getJson(path) {
    return requestJson(path);
}

export default { getJson, requestJson };
