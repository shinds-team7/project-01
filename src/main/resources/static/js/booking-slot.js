/*
 * 예약 요청 화면(booking-request.html) 전용 스크립트.
 *
 * 1) 시간대·날짜 탭투탭 선택
 *    - 칸을 처음 탭하면 그 칸이 시작점(anchor)이 되고, 다시 탭할 때마다 anchor 에서 범위를 다시 잡는다.
 *      두 칸을 골랐다고 화면이 넘어가지 않으므로 얼마든지 고쳐 잡을 수 있다.
 *    - anchor 를 다시 탭하면 선택이 풀린다.
 *    - 범위 안에 예약 불가한 칸(data-pick-selectable="false")이나 끊긴 구간이 있으면
 *      CTA 를 잠그고 이유를 알려준다. 넘어가는 건 오직 하단 CTA 를 눌렀을 때뿐이다.
 *    - 스크립트가 없으면 각 칸의 href 로 서버 탭투탭이 그대로 동작한다.
 *
 * 2) 결제하기 → 토스 결제창 목업
 *    - 반려동물을 고르지 않았으면 결제창을 열지 않고 안내만 띄운다(서버 @NotEmpty 와 같은 조건).
 *    - 목업 승인이 끝나면 예약 요청 폼을 제출한다. 실제 PG 연동은 없다.
 */
(() => {
    "use strict";

    const SLOT_HOURS = 3;
    const MS_PER_HOUR = 60 * 60 * 1000;
    const MS_PER_DAY = 24 * MS_PER_HOUR;

    function formatPrice(value) {
        return value.toLocaleString("ko-KR") + "원";
    }

    // ────────────────────────── 탭투탭 선택 ──────────────────────────
    const picker = document.querySelector("[data-picker]");
    const form = document.querySelector("[data-pick-form]");

    if (picker && form) {
        const mode = picker.dataset.picker; // "hourly" | "package"
        const unitPrice = Number(picker.dataset.unitPrice) || 0;
        const cells = [...picker.querySelectorAll("[data-pick-value]")];
        const startInput = form.querySelector('[data-pick-input="start"]');
        const endInput = form.querySelector('[data-pick-input="end"]');
        const submit = form.querySelector("[data-pick-submit]");
        const priceLabel = form.querySelector("[data-pick-price]");
        const summaryLabel = form.querySelector("[data-pick-summary]");
        const emptyPrice = priceLabel ? priceLabel.textContent : "";
        const emptySummary = summaryLabel ? summaryLabel.textContent : "";

        const isSelectable = (cell) => cell.dataset.pickSelectable === "true";
        const timeOf = (cell, key) => new Date(cell.dataset[key]).getTime();
        const dateOf = (cell) => new Date(cell.dataset.pickDate + "T00:00:00").getTime();

        let anchor = null; // 시작점 인덱스
        let cursor = null; // 마지막으로 탭한 인덱스

        /** 선택 범위를 검사해 화면 표시와 CTA 상태를 한 번에 맞춘다. */
        function paint() {
            const from = anchor === null ? null : Math.min(anchor, cursor);
            const to = anchor === null ? null : Math.max(anchor, cursor);

            const inRange = (index) => from !== null && index >= from && index <= to;
            const picked = cells.filter((cell, index) => inRange(index));
            const blocked = picked.some((cell) => !isSelectable(cell));

            let message = "";
            let price = null;

            if (picked.length > 0 && !blocked) {
                if (mode === "hourly") {
                    const hours = (timeOf(picked[picked.length - 1], "pickEnd") - timeOf(picked[0], "pickStart")) / MS_PER_HOUR;
                    // 사이에 슬롯이 비어 있으면(호스트가 안 만든 구간) 시간 계산이 칸 수와 어긋난다
                    if (hours !== picked.length * SLOT_HOURS) {
                        message = "연속된 시간만 예약할 수 있어요.";
                    } else {
                        const startText = picked[0].dataset.pickStart.slice(11);
                        const endText = picked[picked.length - 1].dataset.pickEnd.slice(11);
                        message = startText + " ~ " + endText + " · 총 " + hours + "시간";
                        price = unitPrice * hours;
                    }
                } else {
                    const nights = (dateOf(picked[picked.length - 1]) - dateOf(picked[0])) / MS_PER_DAY;
                    if (nights + 1 !== picked.length) {
                        message = "연속된 날짜만 예약할 수 있어요.";
                    } else if (nights < 1) {
                        message = "체크아웃 날짜를 한 번 더 탭해주세요. (1박 이상)";
                    } else {
                        message = picked[0].dataset.pickDate + " ~ " + picked[picked.length - 1].dataset.pickDate + " · " + nights + "박";
                        price = unitPrice * nights;
                    }
                }
            } else if (blocked) {
                message = mode === "hourly"
                    ? "예약 불가한 시간이 포함돼 있어요."
                    : "예약 불가한 날짜가 포함돼 있어요.";
            }

            cells.forEach((cell, index) => {
                const selected = inRange(index);
                cell.classList.toggle("is-selected", selected && !blocked);
                cell.classList.toggle("is-invalid", selected && blocked && !isSelectable(cell));
                cell.classList.toggle("is-edge", selected && !blocked && (index === from || index === to));
                if (isSelectable(cell)) cell.setAttribute("aria-pressed", String(selected));
            });

            const valid = price !== null;
            if (startInput) startInput.value = valid ? picked[0].dataset.pickValue : "";
            if (endInput) endInput.value = valid ? picked[picked.length - 1].dataset.pickValue : "";
            if (submit) submit.disabled = !valid;
            if (priceLabel) priceLabel.textContent = valid ? formatPrice(price) : emptyPrice;
            if (summaryLabel) summaryLabel.textContent = message || emptySummary;
        }

        cells.forEach((cell, index) => {
            if (!isSelectable(cell)) return;

            cell.setAttribute("role", "button");
            cell.setAttribute("aria-pressed", "false");
            cell.addEventListener("click", (event) => {
                // 서버 탭투탭 링크(JS 없을 때의 경로)를 막고 화면 안에서만 고른다
                event.preventDefault();

                if (anchor === null) {
                    anchor = index;
                    cursor = index;
                } else if (index === anchor && cursor === anchor) {
                    anchor = null;
                    cursor = null;
                } else {
                    cursor = index;
                }
                paint();
            });
        });

        paint();
    }

    // ────────────────────────── 결제 목업 ──────────────────────────
    const payForm = document.querySelector("[data-pay-form]");
    const payDialog = document.getElementById("toss-pay");

    if (payForm && payDialog) {
        const openButton = payForm.querySelector("[data-pay-open]");
        const errorNotice = payForm.querySelector("[data-pay-error]");
        const confirmButton = payDialog.querySelector("[data-pay-confirm]");
        const confirmLabel = payDialog.querySelector("[data-pay-confirm-label]");
        const closeButton = payDialog.querySelector("[data-pay-close]");

        openButton?.addEventListener("click", (event) => {
            event.preventDefault();

            // petIds 는 서버에서 @NotEmpty 다. 결제창까지 갔다가 검증에서 튕기지 않게 여기서 잡는다.
            const chosen = payForm.querySelectorAll('input[name="petIds"]:checked').length > 0;
            if (errorNotice) errorNotice.hidden = chosen;
            if (!chosen) {
                errorNotice?.scrollIntoView({ behavior: "smooth", block: "center" });
                return;
            }

            if (!payDialog.open) payDialog.showModal();
        });

        closeButton?.addEventListener("click", () => payDialog.close());

        confirmButton?.addEventListener("click", () => {
            confirmButton.disabled = true;
            if (closeButton) closeButton.disabled = true;
            if (confirmLabel) confirmLabel.textContent = "결제 승인 중…";

            // 목업이라 실제 승인 요청은 없다. 승인 연출만 잠깐 보여주고 예약 요청을 제출한다.
            window.setTimeout(() => {
                if (confirmLabel) confirmLabel.textContent = "결제 완료";
                window.setTimeout(() => {
                    payDialog.close();
                    payForm.submit();
                }, 400);
            }, 1000);
        });
    }
})();
