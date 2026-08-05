(() => {
    "use strict";

    const form = document.querySelector("[data-pet-form]");
    if (!form) return;

    document.querySelectorAll("[data-choice-group]").forEach((group) => {
        const input = document.querySelector(`[data-choice-input="${group.dataset.choiceGroup}"]`);
        const buttons = [...group.querySelectorAll("button")];
        buttons.forEach((button) => {
            button.addEventListener("click", () => {
                buttons.forEach((b) => b.classList.toggle("is-active", b === button));
                if (input) input.value = button.dataset.value;
            });
        });
    });

    form.addEventListener("submit", async (event) => {
        event.preventDefault();
        const formData = new FormData(form);
        const payload = {
            name: formData.get("name"),
            breed: formData.get("breed") || null,
            weight: formData.get("weight") ? Number(formData.get("weight")) : null,
            birthYear: formData.get("birthYear") ? Number(formData.get("birthYear")) : null,
            sizeCode: formData.get("sizeCode"),
            gender: formData.get("gender"),
            neutered: formData.get("neutered") === "on",
            note: formData.get("note") || null
        };

        const submitButton = form.querySelector('[type="submit"]');
        if (submitButton) submitButton.disabled = true;

        try {
            const response = await fetch("/addpet", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(payload)
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
