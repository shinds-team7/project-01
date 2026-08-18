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

    function hideAvailability() {
        button.hidden = true;
        clearResult();
    }

    input.addEventListener("input", clearResult);

    button.addEventListener("click", async () => {
        const nickname = input.value.trim();
        clearResult();

        if (!nickname) return;

        try {
            const query = new URLSearchParams({ nickname });
            const response = await fetch(`/my/profile/nickname-availability?${query}`, {
                headers: { Accept: "application/json" }
            });

            if (!response.ok) {
                hideAvailability();
                return;
            }

            const data = await response.json();
            if (typeof data.available !== "boolean" || typeof data.message !== "string") {
                hideAvailability();
                return;
            }

            if (input.value.trim() !== nickname) return;

            result.textContent = data.message;
            result.classList.remove("field-hint", "field-error");
            result.classList.add(data.available ? "field-hint" : "field-error");
            result.hidden = false;
        } catch {
            hideAvailability();
        }
    });
})();
