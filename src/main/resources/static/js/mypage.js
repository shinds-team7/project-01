(() => {
    "use strict";

    const tabs = [...document.querySelectorAll("[data-tab]")];
    const panels = [...document.querySelectorAll("[data-panel]")];
    const toast = document.querySelector("[data-toast]");
    let toastTimer;

    function showToast(message) {
        if (!toast || !message) return;
        window.clearTimeout(toastTimer);
        toast.textContent = message;
        toast.classList.add("is-visible");
        toastTimer = window.setTimeout(() => toast.classList.remove("is-visible"), 2200);
    }

    function selectTab(name, focusTab = false) {
        const selectedTab = tabs.find((tab) => tab.dataset.tab === name);
        const selectedPanel = panels.find((panel) => panel.dataset.panel === name);
        if (!selectedTab || !selectedPanel) return;

        tabs.forEach((tab) => {
            const selected = tab === selectedTab;
            tab.setAttribute("aria-selected", String(selected));
            tab.tabIndex = selected ? 0 : -1;
        });

        panels.forEach((panel) => {
            panel.hidden = panel !== selectedPanel;
        });

        if (focusTab) selectedTab.focus();
        window.history.replaceState(null, "", "#" + name);
    }

    tabs.forEach((tab, index) => {
        tab.tabIndex = tab.getAttribute("aria-selected") === "true" ? 0 : -1;
        tab.addEventListener("click", () => selectTab(tab.dataset.tab));
        tab.addEventListener("keydown", (event) => {
            if (!["ArrowLeft", "ArrowRight", "Home", "End"].includes(event.key)) return;
            event.preventDefault();

            let nextIndex = index;
            if (event.key === "ArrowRight") nextIndex = (index + 1) % tabs.length;
            if (event.key === "ArrowLeft") nextIndex = (index - 1 + tabs.length) % tabs.length;
            if (event.key === "Home") nextIndex = 0;
            if (event.key === "End") nextIndex = tabs.length - 1;
            selectTab(tabs[nextIndex].dataset.tab, true);
        });
    });

    document.querySelectorAll("[data-tab-shortcut]").forEach((button) => {
        button.addEventListener("click", () => {
            selectTab(button.dataset.tabShortcut, true);
            document.querySelector(".dashboard-main")?.scrollIntoView({ behavior: "smooth", block: "start" });
        });
    });

    document.addEventListener("click", (event) => {
        const pending = event.target.closest("[data-pending-feature]");
        if (!pending) return;
        event.preventDefault();
        showToast(pending.dataset.pendingFeature + " 기능은 백엔드 API 연결 후 사용할 수 있어요.");
    });

    const initialTab = window.location.hash.slice(1);
    if (tabs.some((tab) => tab.dataset.tab === initialTab)) {
        selectTab(initialTab);
    } else {
        selectTab("overview");
    }
})();
