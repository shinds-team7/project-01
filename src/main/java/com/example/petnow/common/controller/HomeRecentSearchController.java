package com.example.petnow.common.controller;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

/** 브라우저 세션 안에서만 보관하는 홈 검색어 기록이다. */
@RestController
@RequestMapping("/api/home/recent-searches")
public class HomeRecentSearchController {

    private static final String SESSION_KEY = "homeRecentSearches";
    private static final int MAX_RECENT_SEARCHES = 8;

    @GetMapping
    public List<String> recentSearches(HttpSession session) {
        return getSearches(session);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public List<String> addRecentSearch(@Valid @RequestBody RecentSearchRequest request, HttpSession session) {
        String query = request.query().trim();
        LinkedHashSet<String> deduplicated = new LinkedHashSet<>();
        deduplicated.add(query);
        deduplicated.addAll(getSearches(session));

        List<String> searches = deduplicated.stream().limit(MAX_RECENT_SEARCHES).toList();
        session.setAttribute(SESSION_KEY, searches);
        return searches;
    }

    @SuppressWarnings("unchecked")
    private List<String> getSearches(HttpSession session) {
        Object value = session.getAttribute(SESSION_KEY);
        if (!(value instanceof List<?> values)) {
            return List.of();
        }
        return values.stream().filter(String.class::isInstance).map(String.class::cast).toList();
    }

    public record RecentSearchRequest(
            @NotBlank(message = "검색어를 입력해주세요.")
            @Size(max = 60, message = "검색어는 60자 이하로 입력해주세요.") String query
    ) {}
}
