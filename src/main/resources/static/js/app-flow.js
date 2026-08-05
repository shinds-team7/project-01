(() => {
    "use strict";

    const toast = document.querySelector("[data-toast]");
    let toastTimer;

    function showToast(message) {
        if (!toast || !message) return;
        window.clearTimeout(toastTimer);
        toast.textContent = message;
        toast.classList.add("is-visible");
        toastTimer = window.setTimeout(() => toast.classList.remove("is-visible"), 2200);
    }

    document.addEventListener("click", (event) => {
        const pending = event.target.closest("[data-pending-feature]");
        if (!pending) return;
        event.preventDefault();
        showToast(pending.dataset.pendingFeature + " 기능은 백엔드 API 연결 후 사용할 수 있어요.");
    });

    // Star rating picker: click a star, fills that star and everything before it.
    const starPicker = document.querySelector("[data-star-picker]");
    if (starPicker) {
        const stars = [...starPicker.querySelectorAll("button")];
        const input = document.querySelector("[data-rating-input]");
        const label = document.querySelector("[data-rating-label]");
        const labels = ["별로였어요", "아쉬웠어요", "보통이었어요", "좋았어요", "최고예요"];

        function paint(value) {
            stars.forEach((star, index) => star.classList.toggle("is-active", index < value));
            if (label) label.textContent = value ? labels[value - 1] : "별점을 선택해주세요";
        }

        stars.forEach((star, index) => {
            star.addEventListener("click", () => {
                const value = index + 1;
                if (input) input.value = String(value);
                paint(value);
            });
        });

        paint(Number(input?.value) || 0);
    }

    // Character counter for inputs/textareas with data-count-target
    document.querySelectorAll("[data-count-target]").forEach((field) => {
        const counter = document.querySelector(field.dataset.countTarget);
        if (!counter) return;
        const sync = () => { counter.textContent = String(field.value.length); };
        field.addEventListener("input", sync);
        sync();
    });

    // Suggestion chips that append their text into a linked textarea
    document.querySelectorAll("[data-suggest-for]").forEach((chip) => {
        const target = document.querySelector(chip.dataset.suggestFor);
        if (!target) return;
        chip.addEventListener("click", () => {
            const sep = target.value.trim() ? " " : "";
            target.value = (target.value + sep + chip.textContent.trim()).slice(0, target.maxLength > 0 ? target.maxLength : undefined);
            target.dispatchEvent(new Event("input"));
            target.focus();
        });
    });
})();
