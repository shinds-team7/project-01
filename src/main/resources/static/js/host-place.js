(() => {
    "use strict";

    const form = document.querySelector("[data-place-form]");
    if (!form) return;

    const nameInput = document.querySelector("[data-count-name]");
    const descriptionInput = document.querySelector("[data-count-description]");
    const nameCount = document.querySelector("[data-name-count]");
    const descriptionCount = document.querySelector("[data-description-count]");
    const otherToggle = document.querySelector("[data-other-toggle]");
    const otherField = document.querySelector("[data-other-field]");
    const otherInput = document.querySelector("[data-other-input]");
    const stepIndicators = [...document.querySelectorAll("[data-step-indicator]")];
    const sections = [...document.querySelectorAll("[data-form-section]")];
    const mobileProgress = document.querySelector("[data-mobile-progress]");
    const saveStatus = document.querySelector("[data-save-status]");
    const submitButton = document.querySelector("[data-submit-button]");
    const exitButton = document.querySelector("[data-exit-form]");
    const exitDialog = document.querySelector("[data-exit-dialog]");
    const stayButton = document.querySelector("[data-stay-form]");
    const toast = document.querySelector("[data-form-toast]");
    let dirty = false;
    let toastTimer;

    function showToast(message) {
        if (!toast || !message) return;
        window.clearTimeout(toastTimer);
        toast.textContent = message;
        toast.classList.add("is-visible");
        toastTimer = window.setTimeout(() => toast.classList.remove("is-visible"), 2200);
    }

    function syncCounters() {
        if (nameInput && nameCount) nameCount.textContent = String(nameInput.value.length);
        if (descriptionInput && descriptionCount) descriptionCount.textContent = String(descriptionInput.value.length);
    }

    function syncOtherField() {
        if (!otherToggle || !otherField) return;
        otherField.classList.toggle("is-visible", otherToggle.checked);
        if (!otherToggle.checked && otherInput) otherInput.value = "";
    }

    function hasValue(input) {
        if (input.type === "radio" || input.type === "checkbox") return input.checked;
        return input.value.trim() !== "";
    }

    function sectionIsComplete(section) {
        if (section.dataset.formSection === "service") return true;
        const required = [...section.querySelectorAll("[required]")];
        const radioNames = new Set(required.filter((input) => input.type === "radio").map((input) => input.name));
        const regularInputsComplete = required
            .filter((input) => input.type !== "radio")
            .every(hasValue);
        const radioGroupsComplete = [...radioNames].every((name) =>
            [...form.elements].some((input) => input.name === name && input.checked)
        );
        return regularInputsComplete && radioGroupsComplete;
    }

    function updateStepState(activeName) {
        const activeIndex = sections.findIndex((section) => section.dataset.formSection === activeName);
        stepIndicators.forEach((indicator, index) => {
            const section = sections.find((item) => item.dataset.formSection === indicator.dataset.stepIndicator);
            indicator.classList.toggle("is-current", indicator.dataset.stepIndicator === activeName);
            indicator.classList.toggle("is-complete", Boolean(section && sectionIsComplete(section)));
            if (index < activeIndex && section) indicator.classList.add("is-complete");
        });

        if (mobileProgress && activeIndex >= 0) {
            mobileProgress.style.width = String(((activeIndex + 1) / sections.length) * 100) + "%";
        }
    }

    form.addEventListener("input", () => {
        dirty = true;
        syncCounters();
        const visibleSection = sections.find((section) => {
            const rect = section.getBoundingClientRect();
            return rect.top < window.innerHeight * 0.55 && rect.bottom > window.innerHeight * 0.3;
        });
        if (visibleSection) updateStepState(visibleSection.dataset.formSection);
        if (saveStatus) saveStatus.lastChild.textContent = " 입력 내용을 확인 중";
    });

    otherToggle?.addEventListener("change", () => {
        syncOtherField();
        if (otherToggle.checked) {
            window.setTimeout(() => otherInput?.focus(), 80);
        }
    });

    if ("IntersectionObserver" in window) {
        const observer = new IntersectionObserver((entries) => {
            const activeEntry = entries
                .filter((entry) => entry.isIntersecting)
                .sort((a, b) => b.intersectionRatio - a.intersectionRatio)[0];
            if (activeEntry) updateStepState(activeEntry.target.dataset.formSection);
        }, { rootMargin: "-25% 0px -55% 0px", threshold: [0, 0.15, 0.35] });
        sections.forEach((section) => observer.observe(section));
    }

    exitButton?.addEventListener("click", () => {
        if (!dirty) {
            window.location.assign("/places");
            return;
        }
        if (typeof exitDialog?.showModal === "function") {
            exitDialog.showModal();
        } else {
            showToast("작성 중인 내용이 있어요. 홈으로 이동하기 전 확인해주세요.");
        }
    });

    stayButton?.addEventListener("click", () => exitDialog?.close());
    exitDialog?.addEventListener("click", (event) => {
        if (event.target === exitDialog) exitDialog.close();
    });

    form.addEventListener("submit", (event) => {
        if (!form.checkValidity()) {
            event.preventDefault();
            const firstInvalid = form.querySelector(":invalid");
            firstInvalid?.focus();
            showToast("필수 항목을 확인해주세요.");
            return;
        }

        dirty = false;
        if (submitButton) {
            submitButton.classList.add("is-loading");
            submitButton.disabled = true;
            submitButton.querySelector("span").textContent = "등록 요청 전송 중";
        }
    });

    syncCounters();
    syncOtherField();
    updateStepState("identity");
})();
