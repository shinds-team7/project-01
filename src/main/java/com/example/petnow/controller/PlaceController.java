package com.example.petnow.controller;

import com.example.petnow.common.argument.LoginUser;
import com.example.petnow.common.session.LoginSession;
import com.example.petnow.dto.request.PlaceCreateRequest;
import com.example.petnow.entity.PlaceType;
import com.example.petnow.service.PlaceService;
import jakarta.servlet.http.HttpServletRequest;
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

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("placeCreateRequest", new PlaceCreateRequest());
        addCreateFormAttributes(model);

        return "places/create";
    }

    /**
     * 등록 제출.
     *
     * <p>주소가 {@code POST /places} 가 아니라 {@code /places/create} 인 이유가 있다.
     * 전자는 공개 목록인 {@code GET /places} 와 주소가 같아 인터셉터의 경로 패턴으로
     * 둘을 가를 수 없고, 그러면 이 매핑만 세션을 직접 확인하는 예외가 된다.
     * 주소를 나눠 두면 로그인 판단이 {@code WebConfig} 한곳에 그대로 남는다.
     * ({@code PetController} 의 {@code /pet/new} · {@code /pet/create} 와 같은 모양이다)
     */
    @PostMapping("/create")
    public String create(@LoginUser Long loginUserId,
                         @Valid @ModelAttribute("placeCreateRequest") PlaceCreateRequest request,
                         BindingResult bindingResult,
                         Model model) {
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

    /**
     * 공개 상세. 로그인 여부에 따라 다르게 그리지만 비로그인도 볼 수 있어야 해서
     * {@code @LoginUser}(없으면 401) 가 아니라 "있으면 쓰고 없으면 null" 로 받는다.
     */
    @GetMapping("/{placeId:\\d+}")
    public String detail(@PathVariable Long placeId, Model model, HttpServletRequest request) {
        Long loginUserId = LoginSession.currentUserId(request);

        model.addAttribute("place", placeService.getPlaceDetail(placeId, loginUserId));

        return "places/place-detail";
    }

    private void addCreateFormAttributes(Model model) {
        model.addAttribute("placeTypes", PlaceType.values());
    }
}
