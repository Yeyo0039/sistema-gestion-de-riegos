async function requestJson(path, options = {}) {
    const response = await fetch(path, {
        ...options,
        headers: {
            Accept: "application/json",
            ...(options.body ? { "Content-Type": "application/json" } : {}),
            ...options.headers
        }
    });

    const responseText = await response.text();
    let data = null;
    if (responseText) {
        try {
            data = JSON.parse(responseText);
        } catch {
            data = responseText;
        }
    }

    if (!response.ok) {
        const message = data && typeof data === "object" && data.error
            ? data.error
            : `La API respondió con HTTP ${response.status}`;
        throw new Error(message);
    }

    return data;
}

export function getJson(path) {
    return requestJson(path);
}

export function postJson(path, body) {
    return requestJson(path, {
        method: "POST",
        body: JSON.stringify(body)
    });
}
