/*
 * 홈 "지금 만날 수 있는 이웃 호스트" 의 장소 유형 칩.
 *
 * 칩은 원래 <a href="/home?placeType=..."> 라 눌리면 페이지 전체가 새로고침됐다.
 * 여기서 클릭을 가로채 같은 주소를 fetch 로만 받아 #places 섹션(검색창·칩·결과 카드)만
 * 통째로 바꿔치기한다. JS 가 없거나 fetch 가 실패하면 href 그대로 이동하니 그때도
 * 화면은 똑같이 나온다.
 *
 * #places 섹션을 매번 다시 그리므로 노드 참조를 붙잡아 두지 않고 매번
 * document.getElementById 로 "지금 문서에 붙어 있는" 노드를 다시 찾는다.
 */
(() => {
    "use strict";

    if (!document.getElementById("places")) return;

    async function loadPlaceType(url, pushHistory) {
        const response = await fetch(url, {headers: {"X-Requested-With": "XMLHttpRequest"}});
        if (!response.ok) throw new Error("failed to load nearby results");

        const doc = new DOMParser().parseFromString(await response.text(), "text/html");
        const nextPlaces = doc.getElementById("places");
        const currentPlaces = document.getElementById("places");
        if (!nextPlaces || !currentPlaces) throw new Error("malformed nearby fragment");

        currentPlaces.replaceWith(nextPlaces);
        // home-classic.js 가 최초 로딩 때 한 번 .reveal 에 .is-visible 을 달아 준다.
        // 방금 갈아 끼운 조각은 그 타이밍을 놓치므로 여기서 직접 달아 준다.
        nextPlaces.querySelectorAll(".reveal").forEach((el) => el.classList.add("is-visible"));
        if (pushHistory) window.history.pushState({nearbyUrl: url}, "", url);
    }

    document.addEventListener("click", (event) => {
        const link = event.target.closest("#places .filter-chips .filter-chip");
        if (!link) return;
        event.preventDefault();
        loadPlaceType(link.href, true).catch(() => window.location.assign(link.href));
    });

    window.addEventListener("popstate", () => {
        loadPlaceType(window.location.href, false).catch(() => window.location.reload());
    });
})();
