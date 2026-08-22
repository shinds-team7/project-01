/*
 * 조건 검색 폼의 화면 쪽 거들기. (#7)
 *
 * 홈의 검색 조건 카드와 내 주변의 지도 위 필터 바가 같은 조건을 고르므로 거동도 같다.
 * [data-place-filter] 가 붙은 form 을 찾아 두 가지만 한다.
 *
 *   1. 고른 값을 요약 문구로 되비춘다 — [data-filter-summary="region|date|time|pet"]
 *   2. 여러 날을 고르면 시간 칸을 잠근다 — 잠긴 select 는 제출되지 않는다
 *
 * 화면 잠금은 편의일 뿐이라 서버가 같은 규칙을 다시 검증한다. JS 가 없거나 주소창으로
 * 직접 값을 보내도 "복수 날짜 + 시간" 조합은 서버에서 폼 에러가 된다.
 *
 * 요소는 전부 있을 수도 없을 수도 있다. 화면마다 조건을 묶는 방식이 달라 시간 칸이 별도
 * 다이얼로그일 수도, 상세 필터 안에 섞여 있을 수도 있기 때문이다. 없는 건 건너뛴다.
 */
(() => {
    "use strict";

    const form = document.querySelector("[data-place-filter]");
    if (!form) return;

    const summaryOf = (key) => form.querySelector('[data-filter-summary="' + key + '"]');
    const startDate = form.querySelector("[data-filter-start-date]");
    const endDate = form.querySelector("[data-filter-end-date]");
    const startTime = form.querySelector("#filter-start-time");
    const endTime = form.querySelector("#filter-end-time");
    const timeTrigger = form.querySelector("[data-filter-time-trigger]");
    const timeNotice = form.querySelector("[data-filter-time-notice]");
    const petCount = form.querySelector("[data-filter-pet-count]");

    const checked = (name) => Array.from(form.querySelectorAll('input[name="' + name + '"]:checked'));
    const optionText = (select) => (select.value === "" ? "" : select.options[select.selectedIndex].text);
    const dateText = (value) => {
        const parts = value.split("-");
        return Number(parts[1]) + "월 " + Number(parts[2]) + "일";
    };
    const isMultiDay = () =>
        Boolean(startDate) && Boolean(endDate)
        && startDate.value !== "" && endDate.value !== "" && endDate.value !== startDate.value;
    const summarize = (values, unit, fallback) => {
        if (values.length === 0) return fallback;
        if (values.length === 1) return values[0];
        return values[0] + " 외 " + (values.length - 1) + unit;
    };
    /*
     * 아무것도 안 고른 상태에 적을 문구. 홈 카드는 "지역 선택", 내 주변 칩은 "지역" 이라
     * 화면이 data-filter-placeholder 로 직접 정한다. 안 적었으면 처음에 적혀 있던 글자를 쓴다.
     */
    const placeholders = new Map();
    form.querySelectorAll("[data-filter-summary]").forEach((box) => {
        placeholders.set(box.dataset.filterSummary,
            box.dataset.filterPlaceholder || box.textContent.trim());
    });
    const placeholderOf = (key) => placeholders.get(key) || "";

    /* 요약 자리가 없는 화면도 있다. 있을 때만 쓴다. */
    const setSummary = (key, text) => {
        const box = summaryOf(key);
        if (box) box.textContent = text;
    };

    function lockTimeWhenMultiDay() {
        if (!startTime || !endTime) return;
        const locked = isMultiDay();
        [startTime, endTime].forEach((select) => {
            select.disabled = locked;
            if (locked) select.value = "";
        });
        if (timeTrigger) timeTrigger.disabled = locked;
        if (timeNotice) timeNotice.hidden = !locked;
    }

    function refreshSummaries() {
        setSummary("region",
            summarize(checked("regions").map((input) => input.value), "곳", placeholderOf("region")));

        if (startDate) {
            setSummary("date", startDate.value === ""
                ? placeholderOf("date")
                : isMultiDay()
                    ? dateText(startDate.value) + " ~ " + dateText(endDate.value)
                    : dateText(startDate.value));
        }

        if (startTime && endTime) {
            const from = optionText(startTime);
            const to = optionText(endTime);
            setSummary("time", from !== "" && to !== "" ? from + " ~ " + to : placeholderOf("time"));
        }

        const petNames = checked("petIds")
            .map((input) => input.closest("label").textContent.trim().split(" · ")[0]);
        setSummary("pet", summarize(petNames, "마리", placeholderOf("pet")));
        if (petCount) petCount.textContent = petNames.length + "마리를 골랐어요.";

        /*
         * 상세 필터 칩 하나가 시간·유형·반려동물을 함께 대표하는 화면(내 주변)용.
         * 조건이 몇 개 걸렸는지만 알려 준다. 칩 하나에 셋을 다 적으면 넘친다.
         */
        const detail = summaryOf("detail");
        if (detail) {
            const picked = [
                startTime && endTime && startTime.value !== "" && endTime.value !== "",
                form.querySelector('input[name="placeType"]:checked')
                && form.querySelector('input[name="placeType"]:checked').value !== "",
                checked("petIds").length > 0
            ].filter(Boolean).length;
            detail.textContent = picked === 0
                ? placeholderOf("detail")
                : placeholderOf("detail") + " " + picked;
        }
    }

    form.addEventListener("change", () => {
        lockTimeWhenMultiDay();
        refreshSummaries();
    });
    form.addEventListener("click", (event) => {
        if (event.target.closest("[data-filter-apply]")) refreshSummaries();
    });

    lockTimeWhenMultiDay();
    refreshSummaries();
})();
