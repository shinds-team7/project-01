(() => {
    "use strict";

    function hasInternalReferrer() {
        if (!document.referrer) return false;

        try {
            return new URL(document.referrer).origin === window.location.origin;
        } catch {
            return false;
        }
    }

    document.addEventListener("click", (event) => {
        const backLink = event.target.closest("a[data-history-back]");
        if (!backLink || event.button !== 0 || event.metaKey || event.ctrlKey || event.shiftKey || event.altKey) {
            return;
        }

        // A same-origin referrer means this screen was reached from another
        // application screen, so moving back restores exactly that one step.
        if (!hasInternalReferrer()) return;

        event.preventDefault();
        window.history.back();
    });
})();
