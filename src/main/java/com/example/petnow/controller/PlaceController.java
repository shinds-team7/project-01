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


    @GetMapping("/new")
    public String createForm(Model model, HttpSession session) {
        Long loginUserId = (Long) session.getAttribute(SessionConst.LOGIN_USER_ID);
        if (loginUserId == null) {
            return "redirect:/";
        }

        model.addAttribute("placeCreateRequest", new PlaceCreateRequest());
        addCreateFormAttributes(model);

        return "places/create";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("placeCreateRequest") PlaceCreateRequest request,
                         BindingResult bindingResult,
                         Model model,
                         HttpSession session) {
        Long loginUserId = (Long) session.getAttribute(SessionConst.LOGIN_USER_ID);
        if (loginUserId == null) {
            return "redirect:/";
        }

        if (bindingResult.hasErrors()) {
            addCreateFormAttributes(model);
            return "places/create";
        }

        placeService.createPlace(loginUserId, request);

        return "redirect:/places/success";
    }

    private void addCreateFormAttributes(Model model) {
        model.addAttribute("placeTypes", PlaceType.values());
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


}
