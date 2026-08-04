(() => {
    "use strict";

    const $ = (selector, scope = document) => scope.querySelector(selector);
    const $$ = (selector, scope = document) => [...scope.querySelectorAll(selector)];

    let cards = $$('[data-host-card]');
    const grid = $('[data-host-grid]');
    const resultsLayout = $('[data-results-layout]');
    const emptyResults = $('[data-empty-results]');
    const resultCount = $('[data-result-count]');
    const toastRegion = $('[data-toast-region]');
    const mapPanel = $('[data-map-panel]');
    const mapToggle = $('[data-map-toggle]');
    const mapLabel = $('[data-map-label]');
    const hostSearch = $('[data-host-search]');
    const sortSelect = $('[data-sort]');
    const regionLabel = $('[data-region-label]');
    const scheduleLabel = $('[data-schedule-label]');
    const petLabel = $('[data-pet-label]');
    const apiStatus = $('[data-api-status]');

    const state = {
        filter: "all",
        regions: [],
        favoritesOnly: false,
        mapOpen: false,
        features: {
            places: false,
            map: false,
            bookmarks: false
        },
        favorites: readFavorites()
    };

    let toastTimer;

    function readFavorites() {
        try {
            const saved = JSON.parse(localStorage.getItem("petnow:favorites") || "[]");
            return new Set(Array.isArray(saved) ? saved : []);
        } catch (error) {
            return new Set();
        }
    }

    function saveFavorites() {
        try {
            localStorage.setItem("petnow:favorites", JSON.stringify([...state.favorites]));
        } catch (error) {
            // The interface still works when browser storage is unavailable.
        }
    }

    function showToast(message) {
        if (!toastRegion || !message) return;
        window.clearTimeout(toastTimer);
        toastRegion.textContent = message;
        toastRegion.classList.add("is-visible");
        toastTimer = window.setTimeout(() => toastRegion.classList.remove("is-visible"), 2100);
    }

    const featureNames = {
        places: "장소 검색",
        map: "지도",
        bookmarks: "찜"
    };

    function guardLockedInteraction(event) {
        const alwaysLocked = event.target.closest?.("[data-locked-feature]");
        if (alwaysLocked) {
            event.preventDefault();
            event.stopImmediatePropagation();
            showToast(`${alwaysLocked.dataset.lockedFeature} 기능을 구현 중입니다.`);
            return true;
        }

        const gated = event.target.closest?.("[data-requires-api]");
        const feature = gated?.dataset.requiresApi;
        if (!gated || state.features[feature]) return false;
        event.preventDefault();
        event.stopImmediatePropagation();
        showToast(`${featureNames[feature] || "해당"} 기능을 구현 중입니다.`);
        return true;
    }

    document.addEventListener("click", guardLockedInteraction, true);
    document.addEventListener("submit", guardLockedInteraction, true);
    document.addEventListener("keydown", (event) => {
        if ((event.key === "Enter" || event.key === " ") && event.target.closest?.("[data-requires-api], [data-locked-feature]")) {
            guardLockedInteraction(event);
        }
    }, true);

    function setDialogState(dialog, open) {
        if (!dialog) return;
        if (open) {
            if (typeof dialog.showModal === "function") {
                dialog.showModal();
            } else {
                dialog.setAttribute("open", "");
            }
            document.body.classList.add("is-dialog-open");
        } else {
            if (typeof dialog.close === "function") {
                dialog.close();
            } else {
                dialog.removeAttribute("open");
            }
            if (!$("dialog[open]")) document.body.classList.remove("is-dialog-open");
        }
    }

    function setDefaultDates() {
        const form = $('[data-schedule-form]');
        if (!form) return;
        const start = form.elements.startDate;
        const end = form.elements.endDate;
        const tomorrow = new Date();
        tomorrow.setHours(0, 0, 0, 0);
        tomorrow.setDate(tomorrow.getDate() + 1);
        const afterTomorrow = new Date(tomorrow);
        afterTomorrow.setDate(afterTomorrow.getDate() + 1);
        const toInputDate = (date) => {
            const year = date.getFullYear();
            const month = String(date.getMonth() + 1).padStart(2, "0");
            const day = String(date.getDate()).padStart(2, "0");
            return `${year}-${month}-${day}`;
        };
        const minimum = toInputDate(new Date());
        start.min = minimum;
        end.min = minimum;
        start.value = toInputDate(tomorrow);
        end.value = toInputDate(afterTomorrow);

        start.addEventListener("change", () => {
            end.min = start.value;
            if (!end.value || end.value < start.value) end.value = start.value;
        });
    }

    function formatKoreanDate(value) {
        if (!value) return "";
        const [, month, day] = value.split("-");
        return `${Number(month)}.${Number(day)}`;
    }

    function escapeHtml(value) {
        return String(value ?? "")
            .replaceAll("&", "&amp;")
            .replaceAll("<", "&lt;")
            .replaceAll(">", "&gt;")
            .replaceAll('"', "&quot;")
            .replaceAll("'", "&#039;");
    }

    function safeImageUrl(value) {
        if (!value) return "";
        try {
            const url = new URL(value, window.location.origin);
            return ["http:", "https:"].includes(url.protocol) ? url.href : "";
        } catch (error) {
            return "";
        }
    }

    function normalizePlace(raw, index) {
        const typeValue = String(raw.placeType ?? raw.type ?? "").toUpperCase();
        const typeTag = typeValue.includes("HOUSE") || typeValue.includes("주택")
            ? "house"
            : typeValue.includes("APART") || typeValue.includes("아파트") ? "apartment" : "";
        const tags = Array.isArray(raw.tags) ? raw.tags.map(String) : [];
        if (raw.providesYard || raw.provides_yard) tags.push("마당 있음");
        if (raw.allowsCat || raw.allows_cat) tags.push("고양이 가능");
        if (raw.providesRealtimePhoto || raw.provides_realtime_photo) tags.push("실시간 사진");
        const filterTags = [typeTag];
        if (tags.some((tag) => tag.includes("마당"))) filterTags.push("yard");
        if (tags.some((tag) => tag.includes("고양이") || tag.toLowerCase().includes("cat"))) filterTags.push("cat");

        return {
            id: String(raw.id ?? raw.placeId ?? `place-${index}`),
            title: String(raw.name ?? raw.title ?? "이름을 준비 중인 돌봄 공간"),
            description: String(raw.description ?? "장소 소개가 아직 등록되지 않았어요."),
            region: String(raw.sigungu ?? raw.region ?? raw.address?.sigungu ?? "지역 정보 준비 중"),
            distance: Number(raw.distanceKm ?? raw.distance ?? 999),
            rating: Number(raw.averageRating ?? raw.rating ?? 0),
            reviewCount: Number(raw.reviewCount ?? raw.reviews ?? 0),
            price: Number(raw.hourlyPrice ?? raw.price ?? 0),
            tags: [...new Set(tags)].slice(0, 4),
            filterTags: filterTags.filter(Boolean).join(" "),
            imageUrl: safeImageUrl(raw.imageUrl ?? raw.thumbnailUrl ?? raw.photos?.[0]?.imageUrl),
            availableNow: Boolean(raw.availableNow ?? raw.isAvailable)
        };
    }

    function createPlaceCard(place) {
        const article = document.createElement("article");
        article.className = "host-card reveal is-visible";
        article.dataset.hostCard = "";
        article.dataset.id = place.id;
        article.dataset.filterTags = place.filterTags;
        article.dataset.region = place.region;
        article.dataset.distance = String(place.distance);
        article.dataset.rating = String(place.rating);
        article.dataset.price = String(place.price);

        const imageMarkup = place.imageUrl
            ? `<img src="${escapeHtml(place.imageUrl)}" alt="${escapeHtml(place.title)} 공간 이미지">`
            : "";
        const photoClass = place.imageUrl ? "host-photo" : "host-photo is-file-pending";
        const tagsMarkup = (place.tags.length ? place.tags : ["옵션 정보 준비 중"])
            .map((tag) => `<i>${escapeHtml(tag)}</i>`)
            .join("");
        const priceMarkup = place.price > 0
            ? `<strong>${place.price.toLocaleString("ko-KR")}원</strong><small> / 시간</small>`
            : "<strong>가격 협의</strong>";
        const ratingMarkup = place.rating > 0
            ? `<b>★</b> ${place.rating.toFixed(1)} <small>(${place.reviewCount})</small>`
            : "<small>리뷰 준비 중</small>";

        article.innerHTML = `
            <button class="favorite-button" type="button" data-favorite="${escapeHtml(place.id)}" data-requires-api="bookmarks" aria-label="찜 기능 준비 중" aria-pressed="false">
                <span class="material-symbols-rounded" aria-hidden="true">favorite</span>
            </button>
            <button class="host-card-main" type="button" data-open-detail data-requires-api="places">
                <span class="${photoClass}">
                    ${imageMarkup}
                    <span class="availability${place.availableNow ? "" : " availability-warm"}"><i></i>${place.availableNow ? "오늘 예약 가능" : "일정 확인 필요"}</span>
                </span>
                <span class="host-body">
                    <span class="host-kicker">${escapeHtml(place.region)}${Number.isFinite(place.distance) && place.distance < 999 ? ` · ${place.distance}km` : ""}</span>
                    <span class="host-title-row"><strong>${escapeHtml(place.title)}</strong><span>${ratingMarkup}</span></span>
                    <span class="host-description">${escapeHtml(place.description)}</span>
                    <span class="host-tags">${tagsMarkup}</span>
                    <span class="host-price">${priceMarkup}</span>
                </span>
            </button>`;
        return article;
    }

    function renderApiPlaces(items) {
        if (!grid) return;
        const places = items.map(normalizePlace);
        grid.replaceChildren(...places.map(createPlaceCard));
        cards = $$('[data-host-card]');
        bindCardInteractions();
        updateFavoriteButtons();
        applyFilters();
        if (apiStatus) apiStatus.textContent = `${places.length}개의 실제 장소 데이터를 불러왔습니다.`;
    }

    function unlockPlacesFeature() {
        state.features.places = true;
        $('[data-search-form]')?.classList.remove("is-development");
        $$('.is-development-control[data-requires-api="places"]').forEach((control) => control.classList.remove("is-development-control"));
        $$('[data-requires-api="places"]').forEach((control) => control.setAttribute("aria-disabled", "false"));
        if (hostSearch) {
            hostSearch.readOnly = false;
            hostSearch.placeholder = "동네 또는 호스트 이름 검색";
        }
        if (regionLabel) regionLabel.textContent = "지역을 선택해주세요";
        if (scheduleLabel) scheduleLabel.textContent = "날짜와 시간을 선택해주세요";
        if (petLabel) petLabel.textContent = "반려동물을 선택해주세요";
        const searchButtonLabel = $(".search-submit span:last-child");
        if (searchButtonLabel) searchButtonLabel.textContent = "펫시터 찾기";
        apiStatus?.closest(".api-status")?.classList.add("is-connected");
    }

    async function initializePlacesApi() {
        if (!window.PetNowApi) return;
        const probe = await window.PetNowApi.probePlaces();
        if (!probe.available) {
            $$('[data-requires-api]').forEach((control) => control.setAttribute("aria-disabled", "true"));
            return;
        }
        unlockPlacesFeature();
        renderApiPlaces(probe.items);
    }

    function updateFavoriteButtons() {
        $$('[data-favorite]').forEach((button) => {
            if (!state.features.bookmarks || button.closest(".is-api-placeholder")) {
                button.setAttribute("aria-pressed", "false");
                button.setAttribute("aria-label", "찜 기능 준비 중");
                return;
            }
            const active = state.favorites.has(button.dataset.favorite);
            button.setAttribute("aria-pressed", String(active));
            const cardTitle = $(".host-title-row > strong", button.closest("[data-host-card]"))?.textContent || "호스트";
            button.setAttribute("aria-label", `${cardTitle} ${active ? "찜 해제" : "찜하기"}`);
        });
    }

    function matchesCard(card) {
        const query = (hostSearch?.value || "").trim().toLocaleLowerCase("ko-KR");
        const searchableText = card.textContent.toLocaleLowerCase("ko-KR");
        const matchesQuery = !query || searchableText.includes(query);
        const matchesFilter = state.filter === "all" || card.dataset.filterTags.split(" ").includes(state.filter);
        const matchesRegion = state.regions.length === 0 || state.regions.includes(card.dataset.region);
        const matchesFavorite = !state.favoritesOnly || state.favorites.has(card.dataset.id);
        return matchesQuery && matchesFilter && matchesRegion && matchesFavorite;
    }

    function applyFilters() {
        let visibleCount = 0;
        cards.forEach((card) => {
            const visible = matchesCard(card);
            card.hidden = !visible;
            if (visible) visibleCount += 1;
        });
        if (resultCount) resultCount.textContent = String(visibleCount);
        if (resultsLayout) resultsLayout.hidden = visibleCount === 0;
        if (emptyResults) emptyResults.hidden = visibleCount !== 0;
        return visibleCount;
    }

    function sortCards(value) {
        if (!grid) return;
        const factor = value === "rating" ? -1 : 1;
        const key = value === "rating" ? "rating" : value === "price" ? "price" : "distance";
        cards
            .slice()
            .sort((a, b) => (Number(a.dataset[key]) - Number(b.dataset[key])) * factor)
            .forEach((card) => grid.appendChild(card));
    }

    function setMapView(open) {
        state.mapOpen = open;
        resultsLayout?.classList.toggle("is-map", open);
        if (mapPanel) mapPanel.hidden = !open;
        if (mapToggle) mapToggle.setAttribute("aria-pressed", String(open));
        if (mapLabel) mapLabel.textContent = open ? "목록으로 보기" : "지도로 보기";
    }

    function setActiveMobileNav(key) {
        $$('[data-mobile-nav]').forEach((item) => item.classList.toggle("is-active", item.dataset.mobileNav === key));
    }

    function openDetail(card) {
        const dialog = $("#host-detail-dialog");
        if (!card || !dialog) return;
        const cardImage = $(".host-photo img", card);
        const image = $("[data-detail-image]", dialog);
        const title = $(".host-title-row > strong", card)?.textContent.trim() || "호스트 공간";
        const region = $(".host-kicker", card)?.textContent.trim() || "";
        const rating = ($(".host-title-row > span", card)?.textContent.trim() || "").replace(/^★\s*/, "");
        const description = $(".host-description", card)?.textContent.trim() || "";
        const price = $(".host-price", card)?.innerHTML || "";

        image.src = cardImage?.currentSrc || cardImage?.src || "";
        image.alt = `${title} 공간 사진`;
        $("[data-detail-title]", dialog).textContent = title;
        $("[data-detail-region]", dialog).textContent = region;
        $("[data-detail-rating]", dialog).textContent = `★ ${rating}`;
        $("[data-detail-description]", dialog).textContent = description;
        $("[data-detail-price]", dialog).innerHTML = price;

        const detailTags = $("[data-detail-tags]", dialog);
        detailTags.replaceChildren();
        $$(".host-tags i", card).forEach((tag) => {
            const item = document.createElement("i");
            item.textContent = tag.textContent;
            detailTags.appendChild(item);
        });

        setDialogState(dialog, true);
    }

    function resetFilters() {
        state.filter = "all";
        state.regions = [];
        state.favoritesOnly = false;
        if (hostSearch) hostSearch.value = "";
        $$('[data-filter]').forEach((button) => {
            const active = button.dataset.filter === "all";
            button.classList.toggle("is-active", active);
            button.setAttribute("aria-pressed", String(active));
        });
        $$('[name="district"]').forEach((input) => { input.checked = false; });
        $('[data-favorites-only]')?.setAttribute("aria-pressed", "false");
        $('[data-favorites-only]')?.classList.remove("is-active");
        if (regionLabel) regionLabel.textContent = state.features.places ? "지역을 선택해주세요" : "지역 검색 준비 중";
        applyFilters();
    }

    $$('[data-open-dialog]').forEach((button) => {
        button.addEventListener("click", () => setDialogState(document.getElementById(button.dataset.openDialog), true));
    });

    $$("dialog").forEach((dialog) => {
        dialog.addEventListener("close", () => {
            if (!$("dialog[open]")) document.body.classList.remove("is-dialog-open");
        });
        dialog.addEventListener("click", (event) => {
            if (event.target === dialog) setDialogState(dialog, false);
        });
    });

    const regionForm = $('[data-region-form]');
    regionForm?.addEventListener("change", (event) => {
        if (event.target.name !== "district" || !event.target.checked) return;
        const selected = $$('[name="district"]:checked', regionForm);
        if (selected.length > 3) {
            event.target.checked = false;
            showToast("지역은 최대 3개까지 선택할 수 있어요.");
        }
    });
    regionForm?.addEventListener("submit", (event) => {
        event.preventDefault();
        if (event.submitter?.value === "cancel") {
            setDialogState(regionForm.closest("dialog"), false);
            return;
        }
        state.regions = $$('[name="district"]:checked', regionForm).map((input) => input.value);
        if (regionLabel) {
            regionLabel.textContent = state.regions.length === 0
                ? "지역을 선택해주세요"
                : `${state.regions[0]}${state.regions.length > 1 ? ` 외 ${state.regions.length - 1}곳` : ""}`;
        }
        setDialogState(regionForm.closest("dialog"), false);
        applyFilters();
    });

    const scheduleForm = $('[data-schedule-form]');
    scheduleForm?.addEventListener("submit", (event) => {
        event.preventDefault();
        if (event.submitter?.value === "cancel") {
            setDialogState(scheduleForm.closest("dialog"), false);
            return;
        }
        const startDate = scheduleForm.elements.startDate.value;
        const endDate = scheduleForm.elements.endDate.value;
        const startTime = scheduleForm.elements.startTime.value;
        const endTime = scheduleForm.elements.endTime.value;
        if (!startDate || !endDate) {
            showToast("시작일과 종료일을 모두 선택해주세요.");
            return;
        }
        if (scheduleLabel) {
            const dateText = startDate === endDate
                ? formatKoreanDate(startDate)
                : `${formatKoreanDate(startDate)} – ${formatKoreanDate(endDate)}`;
            scheduleLabel.textContent = `${dateText} · ${startTime}-${endTime}`;
        }
        setDialogState(scheduleForm.closest("dialog"), false);
    });

    const petForm = $('[data-pet-form]');
    petForm?.addEventListener("submit", (event) => {
        event.preventDefault();
        if (event.submitter?.value === "cancel") {
            setDialogState(petForm.closest("dialog"), false);
            return;
        }
        const selectedPets = $$('[name="pet"]:checked', petForm).map((input) => input.value);
        if (selectedPets.length === 0) {
            showToast("돌봄을 받을 반려동물을 한 마리 이상 선택해주세요.");
            return;
        }
        if (petLabel) petLabel.textContent = `${selectedPets[0]}${selectedPets.length > 1 ? ` 외 ${selectedPets.length - 1}마리` : ""}`;
        setDialogState(petForm.closest("dialog"), false);
    });

    $$('[data-filter]').forEach((button) => {
        button.addEventListener("click", () => {
            state.filter = button.dataset.filter;
            $$('[data-filter]').forEach((chip) => {
                const active = chip === button;
                chip.classList.toggle("is-active", active);
                chip.setAttribute("aria-pressed", String(active));
            });
            applyFilters();
        });
    });

    hostSearch?.addEventListener("input", applyFilters);
    sortSelect?.addEventListener("change", () => sortCards(sortSelect.value));
    mapToggle?.addEventListener("click", () => setMapView(!state.mapOpen));

    function bindCardInteractions() {
        $$('[data-favorite]').forEach((button) => {
            if (button.dataset.interactionBound) return;
            button.dataset.interactionBound = "true";
            button.addEventListener("click", () => {
                const id = button.dataset.favorite;
                const cardTitle = $(".host-title-row > strong", button.closest("[data-host-card]"))?.textContent || "호스트";
                if (state.favorites.has(id)) {
                    state.favorites.delete(id);
                    showToast(`${cardTitle}을(를) 찜에서 뺐어요.`);
                } else {
                    state.favorites.add(id);
                    showToast(`${cardTitle}을(를) 찜했어요.`);
                }
                saveFavorites();
                updateFavoriteButtons();
                if (state.favoritesOnly) applyFilters();
            });
        });

        $$('[data-open-detail]').forEach((button) => {
            if (button.dataset.interactionBound) return;
            button.dataset.interactionBound = "true";
            button.addEventListener("click", () => openDetail(button.closest("[data-host-card]")));
        });
    }

    bindCardInteractions();
    $$('[data-map-pin]').forEach((button) => {
        button.addEventListener("click", () => {
            $$('[data-map-pin]').forEach((pin) => pin.classList.remove("is-active"));
            button.classList.add("is-active");
            openDetail(cards.find((card) => card.dataset.id === button.dataset.mapPin));
        });
    });

    $('[data-close-detail]')?.addEventListener("click", () => setDialogState($("#host-detail-dialog"), false));

    $('[data-search-form]')?.addEventListener("submit", async (event) => {
        event.preventDefault();
        const scheduleForm = $('[data-schedule-form]');
        const petForm = $('[data-pet-form]');
        const query = {
            keyword: hostSearch?.value.trim() || "",
            regions: state.regions,
            startDate: scheduleForm?.elements.startDate.value || "",
            endDate: scheduleForm?.elements.endDate.value || "",
            startTime: scheduleForm?.elements.startTime.value || "",
            endTime: scheduleForm?.elements.endTime.value || "",
            pets: $$('[name="pet"]:checked', petForm).map((input) => input.value),
            placeType: state.filter === "all" ? "" : state.filter,
            sort: sortSelect?.value || "distance"
        };

        try {
            const items = await window.PetNowApi.searchPlaces(query);
            renderApiPlaces(items);
            document.getElementById("places")?.scrollIntoView({ behavior: "smooth", block: "start" });
            showToast(items.length > 0 ? `조건에 맞는 호스트 ${items.length}곳을 찾았어요.` : "조건을 조금 넓혀 다시 찾아보세요.");
            setActiveMobileNav("search");
        } catch (error) {
            showToast("검색 결과를 불러오지 못했습니다. 잠시 후 다시 시도해주세요.");
        }
    });

    $('[data-favorites-only]')?.addEventListener("click", (event) => {
        state.favoritesOnly = !state.favoritesOnly;
        event.currentTarget.setAttribute("aria-pressed", String(state.favoritesOnly));
        setActiveMobileNav(state.favoritesOnly ? "favorite" : "search");
        const count = applyFilters();
        document.getElementById("places")?.scrollIntoView({ behavior: "smooth", block: "start" });
        if (state.favoritesOnly && count === 0) showToast("아직 찜한 호스트가 없어요.");
    });

    $('[data-map-shortcut]')?.addEventListener("click", () => {
        setMapView(true);
        setActiveMobileNav("nearby");
        document.getElementById("places")?.scrollIntoView({ behavior: "smooth", block: "start" });
    });

    $('[data-reset-filters]')?.addEventListener("click", resetFilters);

    document.addEventListener("click", (event) => {
        const toastButton = event.target.closest("[data-toast]");
        if (toastButton) showToast(toastButton.dataset.toast);
    });

    $$('a[data-mobile-nav]').forEach((link) => {
        link.addEventListener("click", () => {
            state.favoritesOnly = false;
            $('[data-favorites-only]')?.setAttribute("aria-pressed", "false");
            applyFilters();
            setActiveMobileNav(link.dataset.mobileNav);
        });
    });

    window.addEventListener("scroll", () => {
        $('[data-header]')?.classList.toggle("is-scrolled", window.scrollY > 24);
    }, { passive: true });

    if ("IntersectionObserver" in window) {
        const revealObserver = new IntersectionObserver((entries, observer) => {
            entries.forEach((entry) => {
                if (!entry.isIntersecting) return;
                entry.target.classList.add("is-visible");
                observer.unobserve(entry.target);
            });
        }, { threshold: 0.1, rootMargin: "0px 0px -30px" });
        $$('[class~="reveal"]').forEach((element, index) => {
            element.style.transitionDelay = `${Math.min(index % 3, 2) * 70}ms`;
            revealObserver.observe(element);
        });
    } else {
        $$('[class~="reveal"]').forEach((element) => element.classList.add("is-visible"));
    }

    setDefaultDates();
    updateFavoriteButtons();
    sortCards("distance");
    applyFilters();
    initializePlacesApi();
})();
