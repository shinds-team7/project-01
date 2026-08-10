package com.example.petnow.controller;

import com.example.petnow.common.constant.SessionConst;
import com.example.petnow.dto.request.PetCreateRequest;
import com.example.petnow.dto.request.PetUpdateRequest;
import com.example.petnow.dto.response.PetDetailResponse;
import com.example.petnow.service.PetService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/pet")
@RequiredArgsConstructor
public class PetController {
    private final PetService petService;

    @PostMapping("/create")
    public String addPet(@ModelAttribute PetCreateRequest createRequest, HttpSession session){
        Long userId = (Long) session.getAttribute(SessionConst.LOGIN_USER_ID);
        petService.createPet(userId, createRequest);
        return "mypage";
    }

    @GetMapping("/detail/{petId}")
    public String getDetail(@PathVariable Long petId, Model model, HttpSession session){
        Long userId = (Long) session.getAttribute(SessionConst.LOGIN_USER_ID);
        PetDetailResponse pet = petService.getDetail(userId, petId);
        model.addAttribute("pet",pet);
        // PetDetailResponse 에는 식별자가 없다. 수정 폼이 hidden 으로 되돌려 보내야 하므로
        // 경로 변수로 받은 petId 를 그대로 모델에 넣어 준다.
        model.addAttribute("petId", petId);
        return "mypage/petUpdate";
    }

    @PostMapping("/update")
    public String updatePet(@ModelAttribute PetUpdateRequest updateRequest, HttpSession session){
        Long userId = (Long) session.getAttribute(SessionConst.LOGIN_USER_ID);
        petService.updatePet(userId,updateRequest);
        return "mypage";
    }

    @PostMapping("/delete/{petId}")
    public String deletePet(@PathVariable Long petId, HttpSession session){
        Long userId = (Long) session.getAttribute(SessionConst.LOGIN_USER_ID);
        if (userId == null) {
            return "redirect:/";
        }
        petService.deletePet(userId, petId);
        return "redirect:/mypage";
    }
}
