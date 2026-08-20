(() => {
    "use strict";

    const buttons = document.querySelectorAll("[data-bookmark-toggle]");
    if (buttons.length === 0) {
        return;
    }

    const status = document.querySelector("[data-bookmark-status]");
    const statusText = document.querySelector("[data-bookmark-status-text]");
    const emptyState = document.querySelector("[data-bookmark-empty]");

    const setButtonState = (button, bookmarked) => {
        button.classList.toggle("is-on", bookmarked);
        button.setAttribute("aria-pressed", String(bookmarked));
        button.setAttribute("aria-label", bookmarked ? "찜 취소" : "찜하기");
    };

    const showStatus = (bookmarked) => {
        if (!status || !statusText) {
            return;
        }

        statusText.textContent = bookmarked ? "찜 목록에 추가했어요." : "찜을 취소했어요.";
        status.hidden = false;
    };

    const showEmptyStateIfNeeded = () => {
        if (!emptyState || document.querySelector("[data-bookmark-card]")) {
            return;
        }

        emptyState.hidden = false;
    };

    const toggleBookmark = async (button) => {
        const placeId = button.dataset.placeId;
        if (!placeId || button.disabled) {
            return;
        }

        button.disabled = true;

        try {
            const response = await fetch(`/api/bookmarks/${placeId}/toggle`, {
                method: "POST",
                headers: {
                    "Accept": "application/json"
                }
            });

            if (response.status === 401) {
                window.location.href = "/auth/login";
                return;
            }

            if (!response.ok) {
                throw new Error("Bookmark toggle failed");
            }

            const result = await response.json();
            setButtonState(button, result.bookmarked);
            showStatus(result.bookmarked);

            if (!result.bookmarked && button.hasAttribute("data-remove-card-on-unbookmark")) {
                button.closest("[data-bookmark-card]")?.remove();
                showEmptyStateIfNeeded();
            }
        } catch (error) {
            console.error(error);
            alert("찜 상태를 변경하지 못했어요. 잠시 후 다시 시도해 주세요.");
        } finally {
            button.disabled = false;
        }
    };

    buttons.forEach((button) => {
        button.addEventListener("click", () => toggleBookmark(button));
    });
})();
