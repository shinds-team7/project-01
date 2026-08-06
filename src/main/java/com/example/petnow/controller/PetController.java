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

    /**
     * 반려동물 등록 폼.
     * 마이페이지와 장소 목록의 "아이 등록" 버튼이 이 경로를 가리키고 있었지만
     * 매핑이 없어 404 가 났습니다. 폼은 모델 없이 그려집니다.
     */
    @GetMapping("/new")
    public String createForm(HttpSession session){
        if (session.getAttribute(SessionConst.LOGIN_USER_ID) == null) {
            return "redirect:/";
        }
        return "pet-form";
    }

    @PostMapping("/create")
    public String addPet(@ModelAttribute PetCreateRequest createRequest, HttpSession session){
        Long userId = (Long) session.getAttribute(SessionConst.LOGIN_USER_ID);
        petService.createPet(userId, createRequest);
        // 뷰 이름을 그대로 반환하면 모델이 빈 채로 마이페이지가 그려지고
        // 새로고침 시 등록이 재전송됩니다. PRG 패턴으로 /mypage 에 다시 요청합니다.
        return "redirect:/mypage";
    }

    @GetMapping("/detail/{petId}")
    public String getDetail(@PathVariable Long petId, Model model, HttpSession session){
        Long userId = (Long) session.getAttribute(SessionConst.LOGIN_USER_ID);
        PetDetailResponse pet = petService.getDetail(userId, petId);
        model.addAttribute("pet",pet);
        return "mypage/petUpdate";
    }

    @PostMapping("/update")
    public String updatePet(@ModelAttribute PetUpdateRequest updateRequest, HttpSession session){
        Long userId = (Long) session.getAttribute(SessionConst.LOGIN_USER_ID);
        petService.updatePet(userId,updateRequest);
        return "redirect:/mypage";
    }
}
