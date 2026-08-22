(() => {
    "use strict";

    const header = document.querySelector("[data-header]");

    const reveal = () => document.querySelectorAll(".reveal")
        .forEach((element) => element.classList.add("is-visible"));
    reveal();

    const updateHeader = () => header?.classList.toggle("is-scrolled", window.scrollY > 24);
    updateHeader();
    window.addEventListener("scroll", updateHeader, {passive: true});

})();
