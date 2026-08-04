package com.example.petnow.common.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/frontend")
@RequiredArgsConstructor
public class FrontendCapabilityController {

    private final RequestMappingHandlerMapping handlerMapping;

    @GetMapping("/capabilities")
    public Map<String, Boolean> capabilities() {
        Set<String> getRoutes = handlerMapping.getHandlerMethods().keySet().stream()
                .filter(this::supportsGet)
                .flatMap(info -> info.getPatternValues().stream())
                .collect(Collectors.toSet());
        return Map.of(
                "places", getRoutes.contains("/api/places"),
                "map", getRoutes.contains("/api/places/map"),
                "bookmarks", getRoutes.contains("/api/bookmarks")
        );
    }

    private boolean supportsGet(RequestMappingInfo info) {
        var methods = info.getMethodsCondition().getMethods();
        return methods.isEmpty() || methods.contains(RequestMethod.GET);
    }
}
