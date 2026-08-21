(() => {
    "use strict";

    /*
     * 느린 연결에서 제출 버튼을 두 번 누르면 같은 요청이 두 번 나간다(장소 중복 등록,
     * 리뷰 중복 작성 같은 문제로 이어진다). fragments/base.html 이 이 스크립트를 모든
     * 화면에 걸어 두므로, 폼마다 따로 막을 필요 없이 여기 한 곳에서 처리한다.
     *
     * 캡처링 단계에서 듣는다. 화면 자체 스크립트(app-flow.js 등)가 같은 submit 이벤트를
     * preventDefault 하기 전에 먼저 버튼을 잠가야, 그 사이 두 번째 클릭이 끼어들지 않는다.
     *
     * 서버 검증 실패로 같은 폼이 다시 그려지는 경우는 새 페이지 로드라 버튼이 저절로
     * 풀린다. 뒤로가기(bfcache)로 돌아온 경우만 pageshow 에서 따로 풀어 준다.
     */
    document.addEventListener("submit", (event) => {
        const form = event.target;
        if (!(form instanceof HTMLFormElement)) {
            return;
        }
        form.querySelectorAll('button[type="submit"], input[type="submit"]').forEach((button) => {
            button.disabled = true;
        });
    }, true);

    window.addEventListener("pageshow", (event) => {
        if (!event.persisted) {
            return;
        }
        document.querySelectorAll('button[type="submit"]:disabled, input[type="submit"]:disabled')
                .forEach((button) => {
                    button.disabled = false;
                });
    });
})();
