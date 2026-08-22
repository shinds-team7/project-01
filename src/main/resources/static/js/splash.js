/*
 * 접속 스플래시 페이드아웃.
 *
 * base.html <head> 의 인라인 스크립트가 이미 이번 세션에서 한 번 봤다고 판단하면
 * html 에 petnow-splash-seen 을 붙여 CSS 가 즉시 숨기므로, 여기서는 그 클래스가
 * 없을 때(이번 세션 첫 진입)만 페이드아웃을 진행한다.
 */
(() => {
    "use strict";

    if (document.documentElement.classList.contains("petnow-splash-seen")) return;

    const splash = document.getElementById("app-splash");
    if (!splash) return;

    function dismiss() {
        try {
            sessionStorage.setItem("petnow-splash-seen", "1");
        } catch (e) {
            /* 무시 — sessionStorage 가 막혀 있으면 다음 페이지에서 또 보일 뿐이다. */
        }
        splash.classList.add("is-hidden");
        splash.addEventListener("transitionend", () => splash.remove(), {once: true});
    }

    const minDisplayTime = new Promise((resolve) => setTimeout(resolve, 1000));
    const pageReady = new Promise((resolve) => {
        if (document.readyState === "complete") {
            resolve();
        } else {
            window.addEventListener("load", resolve, {once: true});
        }
    });

    Promise.all([minDisplayTime, pageReady]).then(dismiss);
})();
