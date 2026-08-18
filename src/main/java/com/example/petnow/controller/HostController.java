package com.example.petnow.controller;

import com.example.petnow.common.argument.LoginUser;
import com.example.petnow.entity.ReservationStatus;
import com.example.petnow.service.HostService;
import com.example.petnow.service.ReservationService;
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
    TODO: if 분기문에 Reservation에 대한 model값을 채워주세요
    TODO: else if 분기문에 Review에 대한 model값을 채워주세요
     */
    @GetMapping
    public String dashboard(@LoginUser Long loginUserId,
                            @RequestParam(defaultValue = "places") String tab,
                            @RequestParam(required = false) ReservationStatus status,
                            Model model) {

        if (!tab.equals("booking") && !tab.equals("reviews") && !tab.equals("places")) {
            tab = "places";
        }

        model.addAttribute("tab", tab);

        if ("booking".equals(tab)) {
            model.addAttribute("booking", reservationService.getReservationByHost(loginUserId, status));

        } else if ("reviews".equals(tab)) {

        } else {
            model.addAttribute("places", hostService.getPlacesByUserId(loginUserId));
        }

        return "host/dashboard";
    }
}
