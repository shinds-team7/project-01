(() => {
    "use strict";

    const PLACES_ENDPOINT = "/api/places";
    const CAPABILITIES_ENDPOINT = "/api/frontend/capabilities";

    function appendParam(params, key, value) {
        if (value === undefined || value === null || value === "") return;
        if (Array.isArray(value)) {
            value.filter(Boolean).forEach((item) => params.append(key, item));
            return;
        }
        params.set(key, String(value));
    }

    function extractItems(payload) {
        if (Array.isArray(payload)) return payload;
        if (Array.isArray(payload?.items)) return payload.items;
        if (Array.isArray(payload?.content)) return payload.content;
        if (Array.isArray(payload?.data)) return payload.data;
        return null;
    }

    async function searchPlaces(query = {}) {
        const params = new URLSearchParams();
        Object.entries(query).forEach(([key, value]) => appendParam(params, key, value));
        if (!params.has("size")) params.set("size", "50");

        const response = await fetch(`${PLACES_ENDPOINT}?${params.toString()}`, {
            method: "GET",
            headers: { Accept: "application/json" },
            credentials: "same-origin"
        });

        if (!response.ok) {
            const error = new Error(`places api responded with ${response.status}`);
            error.status = response.status;
            throw error;
        }

        const contentType = response.headers.get("content-type") || "";
        if (!contentType.includes("application/json")) {
            const error = new Error("places api did not return json");
            error.status = 502;
            throw error;
        }

        const payload = await response.json();
        const items = extractItems(payload);
        if (!items) {
            const error = new Error("places api response does not contain a list");
            error.status = 502;
            throw error;
        }
        return items;
    }

    async function probePlaces() {
        try {
            const capabilityResponse = await fetch(CAPABILITIES_ENDPOINT, {
                headers: { Accept: "application/json" },
                credentials: "same-origin"
            });
            if (!capabilityResponse.ok) {
                return { available: false, items: [], reason: "capability endpoint unavailable", status: capabilityResponse.status };
            }
            const capabilities = await capabilityResponse.json();
            if (capabilities.places !== true) {
                return { available: false, items: [], reason: "places api is not implemented", status: 0 };
            }
            const items = await searchPlaces({ size: 12 });
            return { available: true, items };
        } catch (error) {
            return { available: false, items: [], reason: error.message, status: error.status || 0 };
        }
    }

    window.PetNowApi = Object.freeze({
        placesEndpoint: PLACES_ENDPOINT,
        capabilitiesEndpoint: CAPABILITIES_ENDPOINT,
        probePlaces,
        searchPlaces
    });
})();
