package com.example.petnow.controller;

import com.example.petnow.common.argument.LoginUser;
import com.example.petnow.dto.request.PetCreateRequest;
import com.example.petnow.dto.request.PetUpdateRequest;
import com.example.petnow.dto.response.PetDetailResponse;
import com.example.petnow.service.PetService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/pet")
@RequiredArgsConstructor
public class PetController {

    private final PetService petService;

    @GetMapping("/new")
    public String createForm(){
        return "pet-form";
    }

    @PostMapping("/create")
    public String addPet(@LoginUser Long loginUserId, @ModelAttribute PetCreateRequest createRequest){

        petService.createPet(loginUserId, createRequest);
        // 새로고침으로 같은 등록이 반복되지 않도록 PRG 로 마이페이지에 되돌려 보낸다.
        return "redirect:/mypage";
    }

    @GetMapping("/detail/{petId}")
    public String getDetail(@LoginUser Long loginUserId, @PathVariable Long petId, Model model){

        PetDetailResponse pet = petService.getDetail(loginUserId, petId);

        model.addAttribute("pet",pet);
        // PetDetailResponse 에는 식별자가 없다. 수정 폼이 hidden 으로 되돌려 보내야 하므로
        // 경로 변수로 받은 petId 를 그대로 모델에 넣어 준다.
        model.addAttribute("petId", petId);
        return "mypage/petUpdate";
    }

    @PostMapping("/update")
    public String updatePet(@LoginUser Long loginUserId, @ModelAttribute PetUpdateRequest updateRequest){
        petService.updatePet(loginUserId,updateRequest);
        return "redirect:/mypage";
    }

    /** 삭제 확인 화면. 실제 삭제는 아래 POST 가 맡는다. */
    @GetMapping("/delete/{petId}")
    public String deleteForm(@LoginUser Long loginUserId, @PathVariable Long petId, Model model){
        model.addAttribute("pet", petService.getDetail(loginUserId, petId));
        model.addAttribute("petId", petId);
        return "mypage/petDelete";
    }

    @PostMapping("/delete/{petId}")
    public String deletePet(@LoginUser Long loginUserId, @PathVariable Long petId){
        petService.deletePet(loginUserId, petId);
        return "redirect:/mypage";
    }
}
