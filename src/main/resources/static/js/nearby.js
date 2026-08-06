/*
 * 내 주변 호스트(/nearby) 화면 스크립트.
 *
 * 하는 일은 세 가지입니다.
 *   1. 목록 카드 수만큼 지도 위에 가격 핀을 만들어 꽂고, 핀 ↔ 카드를 서로 하이라이트
 *   2. 모바일에서 목록 시트를 손잡이로 끌어올리고 내리기
 *   3. 시트 높이를 CSS 변수(--sheet-h)로 넘겨 nearby.css 가 그리게 하기
 *
 * 찜하기 / 지역·날짜 / 상세 필터는 여기서 처리하지 않습니다.
 * 템플릿에서 data-pending-feature 를 달아두었고, app-flow.js 가 "준비 중" 토스트를 띄웁니다.
 * API가 생기면 템플릿의 해당 속성을 걷어내고 이 파일에 실제 요청을 붙이면 됩니다.
 *
 * 지도 API 연동 시: 아래 PIN_SPOTS(고정 좌표 흉내)를 지우고,
 * 카드 DTO에 위경도를 추가해 지도 SDK의 마커 생성으로 교체하세요.
 */
(() => {
    "use strict";

    const map = document.querySelector("[data-nearby-map]");
    const sheet = document.querySelector("[data-nearby-sheet]");
    const cards = [...document.querySelectorAll(".nearby-card")];

    /* 실제 좌표가 없어 사용하는 임시 배치. 지도 API가 붙으면 통째로 대체됩니다. */
    const PIN_SPOTS = [
        { left: 18, top: 24 }, { left: 58, top: 20 }, { left: 72, top: 37 },
        { left: 15, top: 47 }, { left: 56, top: 53 }, { left: 46, top: 63 },
        { left: 33, top: 18 }, { left: 66, top: 68 }, { left: 26, top: 71 },
        { left: 79, top: 52 }
    ];

    /* 12000 → "1.2만", 5000 → "5,000" */
    function formatPrice(raw) {
        const value = Number(raw);
        if (!Number.isFinite(value) || value <= 0) return "요금 문의";
        if (value < 10000) return value.toLocaleString("ko-KR");
        const man = value / 10000;
        return (Number.isInteger(man) ? man : man.toFixed(1)) + "만";
    }

    function setActive(card, on) {
        if (!card) return;
        card.classList.toggle("is-active", on);
        const pin = card.pinElement;
        if (pin) pin.classList.toggle("is-active", on);
    }

    /* ── 1. 지도 핀 ──────────────────────────────────────────────── */

    const canvas = map ? map.querySelector(".map-canvas") : null;

    if (canvas && cards.length) {
        cards.forEach((card, index) => {
            const spot = PIN_SPOTS[index % PIN_SPOTS.length];
            const pin = document.createElement("button");
            pin.type = "button";
            pin.className = "map-price-pin";
            pin.style.left = spot.left + "%";
            pin.style.top = spot.top + "%";
            pin.textContent = formatPrice(card.dataset.price);

            const name = card.querySelector(".card-name");
            pin.setAttribute("aria-label", (name ? name.textContent.trim() : "호스트") + " 위치");

            pin.addEventListener("click", () => {
                card.scrollIntoView({ behavior: "smooth", block: "center" });
                cards.forEach((other) => setActive(other, other === card));
            });
            pin.addEventListener("mouseenter", () => setActive(card, true));
            pin.addEventListener("mouseleave", () => setActive(card, false));

            card.pinElement = pin;
            card.addEventListener("mouseenter", () => setActive(card, true));
            card.addEventListener("mouseleave", () => setActive(card, false));

            canvas.appendChild(pin);
        });
    }

    /* ── 2~3. 모바일 시트 드래그 ─────────────────────────────────── */

    const grip = document.querySelector("[data-sheet-grip]");
    const split = document.querySelector(".nearby-split");

    if (grip && sheet && split) {
        const MIN_HEIGHT = 120;
        const TOP_GAP = 56;

        function applyHeight(px) {
            const max = split.clientHeight - TOP_GAP;
            const clamped = Math.min(Math.max(px, MIN_HEIGHT), max);
            split.style.setProperty("--sheet-h", clamped + "px");
        }

        function onPointerMove(event) {
            const bottom = split.getBoundingClientRect().bottom;
            applyHeight(bottom - event.clientY);
        }

        function onPointerUp(event) {
            sheet.classList.remove("is-dragging");
            grip.releasePointerCapture?.(event.pointerId);
            window.removeEventListener("pointermove", onPointerMove);
            window.removeEventListener("pointerup", onPointerUp);
        }

        grip.addEventListener("pointerdown", (event) => {
            // 데스크톱 레이아웃에서는 손잡이가 숨겨져 있어 여기까지 오지 않습니다.
            event.preventDefault();
            sheet.classList.add("is-dragging");
            grip.setPointerCapture?.(event.pointerId);
            window.addEventListener("pointermove", onPointerMove);
            window.addEventListener("pointerup", onPointerUp);
        });

        // 키보드로도 시트를 여닫을 수 있게 합니다.
        grip.addEventListener("keydown", (event) => {
            const step = 60;
            const current = sheet.getBoundingClientRect().height;
            if (event.key === "ArrowUp") {
                event.preventDefault();
                applyHeight(current + step);
            } else if (event.key === "ArrowDown") {
                event.preventDefault();
                applyHeight(current - step);
            }
        });
    }
})();
