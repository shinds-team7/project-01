(() => {
    "use strict";

    const input = document.querySelector("[name='nickname']");
    const button = document.querySelector("[data-nickname-check]");
    const result = document.querySelector("[data-nickname-check-result]");

    if (!input || !button || !result) return;

    function clearResult() {
        result.hidden = true;
        result.textContent = "";
        result.classList.remove("field-error");
        result.classList.add("field-hint");
    }

    let latestRequest = 0;

    input.addEventListener("input", () => {
        latestRequest += 1;
        clearResult();
    });

    button.addEventListener("click", async () => {
        const nickname = input.value.trim();
        const requestId = ++latestRequest;
        clearResult();

        if (!nickname) return;

        try {
            const query = new URLSearchParams({ nickname });
            const response = await fetch(`/my/profile/nickname-availability?${query}`, {
                headers: { Accept: "application/json" }
            });

            if (!response.ok) {
                if (requestId === latestRequest) clearResult();
                return;
            }

            const data = await response.json();
            if (typeof data.available !== "boolean" || typeof data.message !== "string") {
                if (requestId === latestRequest) clearResult();
                return;
            }

            if (requestId !== latestRequest || input.value.trim() !== nickname) return;

            result.textContent = data.message;
            result.classList.remove("field-hint", "field-error");
            result.classList.add(data.available ? "field-hint" : "field-error");
            result.hidden = false;
        } catch {
            if (requestId === latestRequest) clearResult();
        }
    });
})();
