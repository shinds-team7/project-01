/*
 * 내 주변의 바텀시트 드래그.
 *
 * 시트를 위로 당기면 목록이 전체화면이 되고, 아래로 내리면 지도가 전체화면이 된다.
 * 멈추는 자리는 세 곳뿐이다.
 *
 *   full — 목록 전체화면 (지도는 필터 바 높이만 남는다)
 *   half — 기본. 지도와 목록을 반씩 본다
 *   peek — 지도 전체화면 (시트는 손잡이와 제목줄만 남는다)
 *
 * 위치는 CSS 변수 --sheet-y 하나로만 준다. 클래스로 세 상태를 각각 그리면 드래그 중의
 * 중간 위치를 표현할 수 없다.
 *
 * 손가락을 뗀 자리에서 가장 가까운 자리로 붙이되, 빠르게 튕기면 거리와 상관없이 그 방향의
 * 다음 자리로 넘어간다. 천천히 조금 움직인 것과 휙 던진 것은 다른 뜻이기 때문이다.
 *
 * 어디를 잡아야 끌리는가.
 *   · 손잡이와 제목줄 — 언제나 끌린다. touch-action:none 이라 브라우저가 가로채지 않는다.
 *   · 목록 안 — 목록이 맨 위일 때 아래로 끌면 시트가 내려가고, 다 펼치기 전에 위로 끌면
 *     시트가 먼저 올라간다. 그 밖에는 목록이 스크롤된다.
 *     다만 목록은 네이티브 스크롤(touch-action:pan-y)이라 터치에서는 브라우저가 먼저
 *     가져가는 경우가 있다. 그래서 확실히 끌리는 자리는 손잡이·제목줄이고, 목록 쪽은 덤이다.
 *
 * 접근성 — 드래그는 포인터가 있어야 하는 조작이라 손잡이를 button 으로 두고 키보드로도
 * 같은 자리를 돈다.
 *
 * 데스크톱(1024px~)에서는 시트가 왼쪽 목록 칸으로 고정된다. 끌 이유가 없으므로 아무것도 하지 않는다.
 * CSS 가 transform 을 none 으로 못박아 두긴 했지만, 그래도 상태를 바꾸면 data-sheet-state 가
 * 따라 바뀌어 목록 스크롤 규칙까지 건드리게 된다. 그래서 화면 폭을 그때그때 확인한다.
 */
(() => {
    "use strict";

    const sheet = document.querySelector("[data-sheet]");
    const screen = document.querySelector(".nearby-screen");
    if (!sheet || !screen) return;

    const handle = sheet.querySelector("[data-sheet-handle]");
    const scroller = sheet.querySelector("[data-sheet-scroll]");

    /* 시트 위쪽이 화면 높이의 몇 %에 오는가. 0 이면 꼭대기, 100 이면 화면 밖. */
    const STOPS = { full: 6, half: 52, peek: 88 };
    const ORDER = ["full", "half", "peek"];
    const LABELS = { full: "목록 접기", half: "목록 펼치기", peek: "목록 펼치기" };

    /* 이 속도(%/ms)를 넘기면 던진 것으로 보고 다음 자리로 넘긴다. */
    const FLING_SPEED = 0.055;
    /* 이만큼 움직이기 전에는 드래그로 치지 않는다. 탭이 드래그로 오인되는 것을 막는다. */
    const DRAG_SLOP_PX = 4;

    /* 데스크톱은 시트가 아니라 고정된 목록 칸이다. CSS 의 분기점과 같은 값이어야 한다. */
    const wideScreen = window.matchMedia("(min-width: 1024px)");
    const fixedLayout = () => wideScreen.matches;

    let state = "half";
    let dragging = false;
    let pointerId = null;
    let startY = 0;
    let startPercent = 0;
    let lastY = 0;
    let lastAt = 0;
    let speed = 0;
    let moved = false;
    /* 방금 끌고 놓았는가. 끌어 놓은 손가락이 카드 위에서 떨어지면 상세로 넘어가는 것을 막는다. */
    let justDragged = false;

    const heightPx = () => screen.clientHeight || 1;
    const toPercent = (px) => (px / heightPx()) * 100;
    const clamp = (percent) => Math.min(STOPS.peek, Math.max(STOPS.full, percent));

    function place(percent) {
        sheet.style.setProperty("--sheet-y", percent + "%");
    }

    /*
     * 상태를 확정한다. 위치뿐 아니라 화면이 지금 무엇을 보여주는 중인지도 같이 알린다.
     * 지도만 보이는 동안 목록이 스크롤되면 손가락이 지도 위에 있는데 목록이 움직인다.
     */
    function settle(next) {
        state = next;
        place(STOPS[next]);
        sheet.dataset.sheetState = next;
        if (handle) {
            handle.setAttribute("aria-expanded", String(next === "full"));
            handle.setAttribute("aria-label", LABELS[next]);
        }
        if (scroller && next === "peek") scroller.scrollTop = 0;
    }

    /* 손을 뗀 자리에서 갈 곳. 던졌으면 방향 우선, 아니면 가장 가까운 자리. */
    function destination(percent, velocity) {
        if (Math.abs(velocity) > FLING_SPEED) {
            const index = ORDER.indexOf(state);
            const next = velocity > 0 ? index + 1 : index - 1;
            return ORDER[Math.min(ORDER.length - 1, Math.max(0, next))];
        }
        return ORDER.reduce((best, key) =>
            Math.abs(STOPS[key] - percent) < Math.abs(STOPS[best] - percent) ? key : best);
    }

    /*
     * 이 지점에서 시작한 드래그를 시트 이동으로 볼지.
     *
     * 목록을 훑는 동작과 시트를 여닫는 동작은 손가락 모양이 같다. 목록 밖(손잡이·제목줄)이면
     * 언제나 시트를 움직이고, 목록 안이면 경계에 있을 때만 시트가 가져간다.
     */
    function grabbable(target, goingDown) {
        if (!scroller || !scroller.contains(target)) return true;
        /* 맨 위에서 더 내릴 곳이 없으면 시트를 내린다. */
        if (goingDown) return scroller.scrollTop <= 0;
        /* 다 펼치기 전에는 목록을 스크롤하기보다 시트를 마저 올리는 게 먼저다. */
        return state !== "full";
    }

    function onDown(event) {
        if (dragging || !event.isPrimary || fixedLayout()) return;
        dragging = true;
        moved = false;
        pointerId = event.pointerId;
        startY = lastY = event.clientY;
        lastAt = event.timeStamp;
        speed = 0;
        startPercent = STOPS[state];
    }

    function onMove(event) {
        if (!dragging || event.pointerId !== pointerId) return;

        const deltaY = event.clientY - startY;
        if (!moved) {
            /* 아직 탭인지 드래그인지 모른다. 이만큼 움직이기 전에는 아무것도 하지 않는다. */
            if (Math.abs(deltaY) < DRAG_SLOP_PX) return;
            if (!grabbable(event.target, deltaY > 0)) {
                dragging = false;
                return;
            }
            moved = true;
            sheet.classList.add("is-dragging");
            /* 여기서부터는 시트가 이 포인터를 독점한다. 목록이 같이 스크롤되면 안 된다. */
            if (sheet.setPointerCapture) sheet.setPointerCapture(pointerId);
        }

        const elapsed = event.timeStamp - lastAt;
        if (elapsed > 0) {
            speed = toPercent(event.clientY - lastY) / elapsed;
            lastY = event.clientY;
            lastAt = event.timeStamp;
        }

        /* 세 자리 바깥으로는 나가지 않는다. 시트가 화면 위로 사라지거나 아래로 빠지면 안 된다. */
        place(clamp(startPercent + toPercent(deltaY)));
        if (event.cancelable) event.preventDefault();
    }

    function onUp(event) {
        if (!dragging || event.pointerId !== pointerId) return;
        dragging = false;
        pointerId = null;

        if (!moved) return;
        moved = false;
        sheet.classList.remove("is-dragging");
        settle(destination(clamp(startPercent + toPercent(event.clientY - startY)), speed));

        /*
         * 끌어 놓은 직후 click 이 한 번 따라온다. 손가락이 카드 위에서 떨어졌다면 그대로 두면
         * 상세로 넘어간다. 이번 한 번만 삼키고 곧바로 되돌린다 — 계속 막으면 카드를 못 누른다.
         */
        justDragged = true;
        window.setTimeout(() => { justDragged = false; }, 0);
    }

    sheet.addEventListener("pointerdown", onDown);
    sheet.addEventListener("pointermove", onMove);
    sheet.addEventListener("pointerup", onUp);
    sheet.addEventListener("pointercancel", onUp);

    /* 잡아채는 단계에서 막아야 카드의 링크까지 가지 않는다. */
    sheet.addEventListener("click", (event) => {
        if (!justDragged) return;
        event.preventDefault();
        event.stopPropagation();
    }, true);

    /*
     * 손잡이를 누르면(또는 키보드로 고르면) 다음 자리로. 다 펼쳐져 있으면 기본으로 되돌아온다.
     * 드래그로 끝난 경우의 click 은 위에서 이미 삼켰으므로 여기까지 오지 않는다.
     */
    if (handle) {
        handle.addEventListener("click", () => {
            if (fixedLayout()) return;
            settle(state === "full" ? "half" : "full");
        });
    }

    /* 화면을 돌리면 %가 가리키는 픽셀이 달라진다. 상태는 그대로 두고 자리만 다시 잡는다. */
    window.addEventListener("resize", () => place(STOPS[state]));

    settle("half");
})();
