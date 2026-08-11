package com.example.petnow.controller;

import com.example.petnow.common.constant.SessionConst;
import com.example.petnow.dto.request.PlaceCreateRequest;
import com.example.petnow.entity.PlaceType;
import com.example.petnow.service.HostService;
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
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/host")
@RequiredArgsConstructor
public class HostController {

    private final HostService hostService;

    @GetMapping("/create")
    public String createForm(Model model, HttpSession session) {
        Long loginUserId = 1L;
//        Long loginUserId = (Long) session.getAttribute(SessionConst.LOGIN_USER_ID);
        if (loginUserId == null) {
            return "redirect:/";
        }

        model.addAttribute("placeCreateRequest", new PlaceCreateRequest());
        addCreateFormAttributes(model);

        return "host/create";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("placeCreateRequest") PlaceCreateRequest requestDTO,
                         BindingResult bindingResult,
                         Model model,
                         HttpSession session) {
        Long loginUserId = 1L;
//        Long loginUserId = (Long) session.getAttribute(SessionConst.LOGIN_USER_ID);
        if (loginUserId == null) {
            return "redirect:/";
        }

        if (bindingResult.hasErrors()) {
            addCreateFormAttributes(model);
            return "host/create";
        }

        hostService.createPlace(loginUserId, requestDTO);

        return "redirect:/host/success";
    }

    @GetMapping("/success")
    public String createSuccess() {
        return "host/success";
    }

    private void addCreateFormAttributes(Model model) {
        model.addAttribute("placeTypes", PlaceType.values());
    }

    /**
     * 호스트 홈. 프로토타입의 HOST HOME 화면.
     *
     * <p>이슈 #179 로 manage.html 을 dashboard.html 로 교체했다.
     * 예약 요청·리뷰 탭은 호스트용 조회 API 가 붙기 전까지 빈 상태로 그려진다.
    */
    @GetMapping
    public String dashboard(@RequestParam(defaultValue = "places") String tab,
                            Model model,
                            HttpSession session) {
        Long loginUserId = (Long) session.getAttribute(SessionConst.LOGIN_USER_ID);
        if (loginUserId == null) {
            return "redirect:/";
        }

        if (!tab.equals("booking") && !tab.equals("reviews") && !tab.equals("places")) {
            tab = "places";
        }

        model.addAttribute("tab", tab);
        model.addAttribute("places", hostService.getPlacesByUserId(loginUserId));

        return "host/dashboard";
    }

    /**
     * 장소 삭제. 소유자 검증은 서비스가 UPDATE 의 WHERE 절로 처리한다.
     *
     * <p>PRG 로 돌려보내 새로고침 때 삭제가 다시 나가지 않게 한다.
     */
    @PostMapping("/places/{placeId}/delete")
    public String deletePlace(@PathVariable Long placeId, HttpSession session) {
        Long loginUserId = (Long) session.getAttribute(SessionConst.LOGIN_USER_ID);
        if (loginUserId == null) {
            return "redirect:/";
        }

        hostService.deletePlace(loginUserId, placeId);

        return "redirect:/host";
    }
}
