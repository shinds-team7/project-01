/*
 * 이미지 선택 · 미리보기 공통 스크립트 (#234)
 *
 * 프로필 수정 · 반려동물 등록/수정 · 장소 등록/수정 · 리뷰 작성이 같은 동작과
 * 같은 안내 문구를 쓰도록 한 곳에 모았다. 화면마다 다시 짜지 않는다.
 *
 * ── 주의 ───────────────────────────────────────────────────────────
 * 여기서 하는 검증은 안내용이다. 잘못 고른 파일을 선택에서 빼주고 이유를 알려줄
 * 뿐, 서버 검증을 대체하지 않는다. 확장자 · 용량 · 장수는 서버에서 다시 본다.
 *
 * ── 쓰는 법 ─────────────────────────────────────────────────────────
 * 화면 템플릿에 아래 마크업을 넣고 스크립트를 걸면 된다.
 *
 *   <div class="image-upload" data-image-upload data-max-files="5" data-max-size-mb="5">
 *       <input class="image-upload-input" type="file" id="photos" name="photos" multiple
 *              accept="image/jpeg,image/png,image/webp" data-image-upload-input>
 *       <label class="image-upload-trigger" for="photos">
 *           <span class="ico" aria-hidden="true">add_a_photo</span>
 *           <b data-image-upload-label>사진 추가</b>
 *           <small data-image-upload-guide></small>
 *       </label>
 *       <ul class="image-upload-list" data-image-upload-list></ul>
 *       <p class="image-upload-error" role="alert" data-image-upload-error></p>
 *   </div>
 *
 *   <link rel="stylesheet" th:href="@{/css/image-upload.css}">  <!-- base.html 에 이미 있다 -->
 *   <script th:src="@{/js/image-upload.js}"></script>
 *
 * data-max-files     최대 장수 (기본 1)
 * data-max-size-mb   장당 최대 용량 MB (기본 5)
 * data-max-total-mb  한 번에 보낼 수 있는 전체 용량 MB (기본 55)
 *
 * ── 기준값은 서버를 따라간다 ─────────────────────────────────────────
 * 화면에 직접 숫자를 정하지 말고 서버 정책을 그대로 옮긴다.
 *
 *   data-max-files    common/storage/ImageCategory 의 maxCount
 *                     USER 1 · PET 1 · PLACE 10 · REVIEW 5
 *   data-max-size-mb  application.yaml 의 spring.servlet.multipart.max-file-size (5MB)
 *   data-max-total-mb application.yaml 의 max-request-size (55MB)
 *
 * 55MB 는 가장 장수가 많은 장소(10장 × 5MB = 50MB)에 텍스트 필드와 boundary 오버헤드를
 * 더한 값이다. 예전에 30MB 였을 때는 각 장이 전부 규격 안인데도 요청이 통째로 거부되는
 * 구간이 있었다 (#240). 그래도 총량 검사는 계속 필요하다 — 한도가 무엇이든 서버에서
 * 413 을 받고 나서야 알게 되면 늦으므로 고르는 시점에 미리 걸러 준다.
 * 서버 쪽 값이 바뀌면 여기 넘기는 값도 같이 고친다.
 *
 * [data-image-upload-guide] 요소에는 "JPG · PNG · WEBP · 장당 5MB 이하 · 최대 5장"
 * 안내가 자동으로 채워진다. 직접 써 둔 문구가 있으면 그대로 둔다.
 * input 이 label 보다 앞에 와야 포커스 링(.image-upload-input:focus-visible ~ …)이 붙는다.
 *
 * 파일을 담아 보내는 form 에는 enctype="multipart/form-data" 를 반드시 지정한다.
 */
(() => {
    "use strict";

    // accept 속성에 넣는 값. 서버 ImageType 화이트리스트와 같다.
    const ACCEPT_TYPES = ["image/jpeg", "image/png", "image/webp"];
    // 검사용. 일부 환경이 jpg 를 image/jpg 로 보고하는데 서버 ImageType.JPEG 도 이를 받는다.
    // 여기서만 막으면 서버가 받아 줄 파일을 화면이 먼저 거절하는 꼴이 된다.
    const ALLOWED_TYPES = [...ACCEPT_TYPES, "image/jpg"];
    const ALLOWED_EXTENSIONS = ["jpg", "jpeg", "png", "webp"];
    const TYPE_GUIDE = "JPG · PNG · WEBP";
    const DEFAULT_MAX_FILES = 1;
    const DEFAULT_MAX_SIZE_MB = 5;
    const DEFAULT_MAX_TOTAL_MB = 55;

    // input.files 는 직접 못 고친다. DataTransfer 로 FileList 를 새로 만들어 넣는다.
    const supportsDataTransfer = typeof DataTransfer === "function";

    function isAllowedType(file) {
        if (file.type) return ALLOWED_TYPES.includes(file.type);
        // type 이 빈 문자열로 오는 환경이 있다. 그때만 확장자로 판단한다.
        return ALLOWED_EXTENSIONS.includes(file.name.split(".").pop().toLowerCase());
    }

    function setup(root) {
        const input = root.querySelector("[data-image-upload-input]");
        const list = root.querySelector("[data-image-upload-list]");
        if (!input || !list) return;

        const errorBox = root.querySelector("[data-image-upload-error]");
        const guide = root.querySelector("[data-image-upload-guide]");
        const triggerLabel = root.querySelector("[data-image-upload-label]");

        const maxFiles = Math.max(1, Number(root.dataset.maxFiles) || DEFAULT_MAX_FILES);
        const maxSizeMb = Number(root.dataset.maxSizeMb) || DEFAULT_MAX_SIZE_MB;
        const maxSize = maxSizeMb * 1024 * 1024;
        const maxTotalMb = Number(root.dataset.maxTotalMb) || DEFAULT_MAX_TOTAL_MB;
        const maxTotal = maxTotalMb * 1024 * 1024;

        // 마크업에서 빠뜨렸어도 서버 기준과 어긋나지 않게 여기서 맞춰준다.
        if (maxFiles > 1) input.multiple = true;
        root.classList.toggle("is-single", maxFiles === 1);
        if (!input.getAttribute("accept")) input.setAttribute("accept", ACCEPT_TYPES.join(","));

        if (guide && !guide.textContent.trim()) {
            guide.textContent = `${TYPE_GUIDE} · 장당 ${maxSizeMb}MB 이하`
                + (maxFiles > 1 ? ` · 최대 ${maxFiles}장` : "");
        }

        // 화면에 보이는 목록. input.files 와 늘 같은 내용을 유지한다.
        let selected = [];

        function showError(message) {
            if (errorBox) errorBox.textContent = message;
            root.classList.toggle("has-error", Boolean(message));
        }

        function syncInput() {
            if (!supportsDataTransfer) return;
            const transfer = new DataTransfer();
            selected.forEach((entry) => transfer.items.add(entry.file));
            input.files = transfer.files;
        }

        function render() {
            list.textContent = "";

            selected.forEach((entry, index) => {
                const item = document.createElement("li");
                item.className = "image-upload-item";

                const image = document.createElement("img");
                image.src = entry.url;
                image.alt = `${entry.file.name} 미리보기`;
                item.appendChild(image);

                const remove = document.createElement("button");
                remove.type = "button";
                remove.className = "image-upload-remove";
                remove.setAttribute("aria-label", `${entry.file.name} 선택 취소`);
                const icon = document.createElement("span");
                icon.className = "ico";
                icon.setAttribute("aria-hidden", "true");
                icon.textContent = "close";
                remove.appendChild(icon);
                remove.addEventListener("click", () => removeAt(index));
                item.appendChild(remove);

                list.appendChild(item);
            });

            root.classList.toggle("is-empty", selected.length === 0);
            root.classList.toggle("is-full", selected.length >= maxFiles);
            if (triggerLabel) {
                // triggerLabel.textContent = maxFiles === 1 && selected.length ? "사진 변경" : "사진 추가";
                triggerLabel.textContent = "사진 선택";
            }
        }

        function revoke(entries) {
            entries.forEach((entry) => URL.revokeObjectURL(entry.url));
        }

        function removeAt(index) {
            revoke(selected.splice(index, 1));
            syncInput();
            render();
            showError("");
            // 지운 버튼과 함께 포커스가 사라지지 않도록 선택 컨트롤로 돌려준다.
            input.focus();
        }

        function toEntries(files) {
            return files.map((file) => ({ file, url: URL.createObjectURL(file) }));
        }

        // 고른 파일 중 기준에 맞는 것만 남기고, 왜 빠졌는지 한 줄로 알려준다.
        function apply(picked) {
            const wrongType = picked.filter((file) => !isAllowedType(file));
            const tooBig = picked.filter((file) => isAllowedType(file) && file.size > maxSize);
            const kept = picked.filter((file) => isAllowedType(file) && file.size <= maxSize);

            // 한 장만 받는 화면은 새로 고른 파일로 갈아끼운다.
            const replacing = maxFiles === 1;
            const withinCount = kept.slice(0, Math.max(0, replacing ? 1 : maxFiles - selected.length));

            // 장수를 지켜도 요청 전체 용량에 걸릴 수 있다.
            // 서버에서 413 을 받기 전에 여기서 먼저 잘라낸다.
            const added = [];
            const overTotal = [];
            let totalBytes = replacing ? 0 : selected.reduce((sum, entry) => sum + entry.file.size, 0);

            withinCount.forEach((file) => {
                if (totalBytes + file.size > maxTotal) {
                    overTotal.push(file);
                    return;
                }
                totalBytes += file.size;
                added.push(file);
            });

            if (added.length) {
                if (replacing) revoke(selected);
                selected = (replacing ? [] : selected).concat(toEntries(added));
            }

            // 걸러낸 파일이 input 에 남아 그대로 전송되지 않도록 늘 다시 맞춘다.
            syncInput();
            render();

            const reasons = [];
            if (wrongType.length) {
                reasons.push(wrongType.length === 1
                    ? `${wrongType[0].name} 은 올릴 수 없어요. ${TYPE_GUIDE} 만 됩니다.`
                    : `${TYPE_GUIDE} 가 아닌 ${wrongType.length}장은 제외했어요.`);
            }
            if (tooBig.length) {
                reasons.push(tooBig.length === 1
                    ? `${tooBig[0].name} 은 ${maxSizeMb}MB 를 넘어요.`
                    : `${maxSizeMb}MB 를 넘는 ${tooBig.length}장은 제외했어요.`);
            }
            if (kept.length > withinCount.length) {
                reasons.push(`사진은 최대 ${maxFiles}장까지 올릴 수 있어요.`);
            }
            if (overTotal.length) {
                reasons.push(`한 번에 올릴 수 있는 용량 ${maxTotalMb}MB 를 넘어 ${overTotal.length}장은 제외했어요.`);
            }
            showError(reasons.join(" "));
        }

        function clearAll() {
            revoke(selected);
            selected = [];
            render();
            showError("");
        }

        input.addEventListener("change", () => {
            const picked = [...input.files];
            if (!picked.length) return;

            if (!supportsDataTransfer) {
                // input.files 를 되돌릴 수 없는 환경이다.
                // 미리보기만 보여주고 걸러내는 일은 서버에 맡긴다.
                revoke(selected);
                selected = toEntries(picked);
                render();
                showError("");
                return;
            }

            apply(picked);
        });

        // form 을 되돌리면 미리보기도 함께 비운다. reset 이 끝난 뒤에 정리한다.
        if (input.form) {
            input.form.addEventListener("reset", () => window.setTimeout(clearAll, 0));
        }

        render();
    }

    document.querySelectorAll("[data-image-upload]").forEach(setup);
})();
