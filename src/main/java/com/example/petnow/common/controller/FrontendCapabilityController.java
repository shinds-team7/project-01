package com.example.petnow.common.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.Map;

@RestController
@RequestMapping("/api/frontend")
@RequiredArgsConstructor
public class FrontendCapabilityController {

    private final RequestMappingHandlerMapping handlerMapping;

    @GetMapping("/capabilities")
    public Map<String, Boolean> capabilities() {
        return Map.of(
                "places", hasGetRoute("/api/places"),
                "map", hasGetRoute("/api/places/map"),
                "bookmarks", hasGetRoute("/api/bookmarks")
        );
    }

    private boolean hasGetRoute(String route) {
        return handlerMapping.getHandlerMethods().keySet().stream()
                .filter(this::supportsGet)
                .flatMap(info -> info.getPatternValues().stream())
                .anyMatch(route::equals);
    }

    private boolean supportsGet(RequestMappingInfo info) {
        var methods = info.getMethodsCondition().getMethods();
        return methods.isEmpty() || methods.contains(RequestMethod.GET);
    }
}
