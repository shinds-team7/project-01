package com.example.petnow.controller;

import com.example.petnow.common.constant.SessionConst;
import com.example.petnow.entity.ReservationStatus;
import com.example.petnow.service.HostService;
import com.example.petnow.service.ReservationService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/host")
@RequiredArgsConstructor
public class HostController {

    private final HostService hostService;
    private final ReservationService reservationService;

    /*
    if 분기문에 Reservation에 대한 model값을 채워주세요
    else if 분기문에 Review에 대한 model값을 채워주세요
     */
    @GetMapping
    public String dashboard(@RequestParam(defaultValue = "booking") String tab,
                            Model model,
                            HttpSession session) {
        Long loginUserId = (Long) session.getAttribute(SessionConst.LOGIN_USER_ID);
        if (loginUserId == null) {
            return "redirect:/";
        }

        if (!tab.equals("booking") && !tab.equals("reviews") && !tab.equals("places")) {
            tab = "booking";
        }

        model.addAttribute("tab", tab);

        if ("booking".equals(tab)) {

        } else if ("reviews".equals(tab)) {

        } else {
            model.addAttribute("places", hostService.getPlacesByUserId(loginUserId));
        }

        return "host/dashboard";
    }
}
