(() => {
    "use strict";

    document.querySelectorAll("[data-password-toggle]").forEach((toggle) => {
        const input = document.getElementById(toggle.getAttribute("aria-controls"));
        const icon = toggle.querySelector("[data-password-icon]");
        if (!input || !icon) return;

        toggle.addEventListener("click", () => {
            const showing = input.type === "text";
            input.type = showing ? "password" : "text";
            toggle.setAttribute("aria-pressed", String(!showing));
            toggle.setAttribute("aria-label", showing ? "비밀번호 표시" : "비밀번호 숨기기");
            icon.textContent = showing ? "visibility" : "visibility_off";
        });
    });
})();
