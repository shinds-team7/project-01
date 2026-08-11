/*
 * <dialog class="modal"> 을 여닫는다.
 *
 *   <button data-modal-open aria-controls="place-delete-3">삭제</button>
 *   <dialog class="modal" id="place-delete-3">
 *     <button data-modal-close>취소</button>
 *   </dialog>
 *
 * 문서 전체에 위임해서 걸므로 카드가 몇 개든, 나중에 붙어도 그대로 동작한다.
 * JS 가 없으면 모달이 열리지 않을 뿐 파괴적인 동작이 실행되지는 않는다.
 */
(() => {
    "use strict";

    document.addEventListener("click", (event) => {
        const opener = event.target.closest("[data-modal-open]");
        if (opener) {
            const dialog = document.getElementById(opener.getAttribute("aria-controls"));
            // showModal 은 이미 열린 dialog 에 다시 부르면 예외가 난다
            if (dialog && !dialog.open) dialog.showModal();
            return;
        }

        const closer = event.target.closest("[data-modal-close]");
        if (closer) closer.closest("dialog")?.close();
    });
})();
