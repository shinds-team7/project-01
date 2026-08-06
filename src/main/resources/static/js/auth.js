(() => {
    "use strict";

    const toast = document.querySelector("[data-auth-toast]");
    let toastTimer;

    function showToast(message) {
        if (!toast || !message) return;
        window.clearTimeout(toastTimer);
        toast.textContent = message;
        toast.classList.add("is-visible");
        toastTimer = window.setTimeout(() => toast.classList.remove("is-visible"), 2200);
    }

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

    document.querySelectorAll("[data-kakao-button]").forEach((button) => {
        button.addEventListener("click", () => showToast("업데이트 예정입니다"));
    });
})();
