package com.example.petnow.controller;

import com.example.petnow.common.constant.SessionConst;
import com.example.petnow.dto.request.PlaceCreateRequest;
import com.example.petnow.entity.PlaceType;
import com.example.petnow.service.PlaceService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/places")
@RequiredArgsConstructor
public class PlaceController {

    private final PlaceService placeService;

    // 등록 폼 진입은 인터셉터(WebConfig 의 "/places/new")가 막는다.
    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("placeCreateRequest", new PlaceCreateRequest());
        addCreateFormAttributes(model);

        return "places/create";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("placeCreateRequest") PlaceCreateRequest request,
                         BindingResult bindingResult,
                         Model model,
                         HttpSession session) {
        // 이 매핑만 세션을 직접 본다. 주소가 공개 목록인 GET /places 와 같아 인터셉터의
        // 경로 패턴으로는 둘을 가를 수 없기 때문이다. (진입점 /places/new 는 인터셉터가 막는다)
        Long loginUserId = (Long) session.getAttribute(SessionConst.LOGIN_USER_ID);
        if (loginUserId == null) {
            return "redirect:/auth/login";
        }

        if (bindingResult.hasErrors()) {
            addCreateFormAttributes(model);
            return "places/create";
        }

        placeService.createPlace(loginUserId, request);

        return "redirect:/places/success";
    }

    @GetMapping("/success")
    public String createSuccess() {
        return "places/success";
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("places", placeService.getPublishedPlaces());

        return "places/list";
    }

    @GetMapping("/{placeId:\\d+}")
    public String detail(@PathVariable Long placeId, Model model, HttpSession session) {
        Long loginUserId = (Long) session.getAttribute(SessionConst.LOGIN_USER_ID);

        model.addAttribute("place", placeService.getPlaceDetail(placeId, loginUserId));

        return "places/place-detail";
    }

    private void addCreateFormAttributes(Model model) {
        model.addAttribute("placeTypes", PlaceType.values());
    }
}
