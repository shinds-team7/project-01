(() => {
    "use strict";

    const form = document.querySelector("[data-pet-form]");
    if (!form) return;

    document.querySelectorAll("[data-choice-group]").forEach((group) => {
        const input = document.querySelector(`[data-choice-input="${group.dataset.choiceGroup}"]`);
        const buttons = [...group.querySelectorAll("button")];
        buttons.forEach((button) => {
            button.addEventListener("click", () => {
                buttons.forEach((b) => {
                    b.classList.toggle("is-active", b === button);
                    b.setAttribute("aria-pressed", String(b === button));
                });
                if (input) input.value = button.dataset.value;
            });
        });
    });

    form.addEventListener("submit", async (event) => {
        event.preventDefault();
        const formData = new FormData(form);
        // PetController 는 @ModelAttribute PetCreateRequest 를 받으므로 폼 인코딩으로 보낸다.
        const params = new URLSearchParams();
        const put = (key, value) => {
            if (value !== null && value !== undefined && value !== "") params.set(key, value);
        };
        put("name", formData.get("name"));
        put("weight", formData.get("weight"));
        put("birthYear", formData.get("birthYear"));
        put("size", formData.get("size"));
        put("sex", formData.get("sex"));
        put("note", formData.get("note"));
        params.set("neutered", String(formData.get("neutered") === "on"));

        const submitButton = form.querySelector('[type="submit"]');
        if (submitButton) submitButton.disabled = true;

        try {
            const response = await fetch("/pet/create", {
                method: "POST",
                headers: { "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8" },
                body: params
            });
            if (!response.ok) throw new Error("request failed");
            window.location.href = "/mypage#pets";
        } catch (error) {
            if (submitButton) submitButton.disabled = false;
            const toast = document.querySelector("[data-toast]");
            if (toast) {
                toast.textContent = "등록에 실패했어요. 다시 시도해주세요.";
                toast.classList.add("is-visible");
                window.setTimeout(() => toast.classList.remove("is-visible"), 2200);
            }
        }
    });
})();
