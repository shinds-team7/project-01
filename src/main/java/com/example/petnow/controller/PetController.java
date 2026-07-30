package com.example.petnow.controller;

import com.example.petnow.dto.request.PetCreateRequest;
import com.example.petnow.dto.request.PetUpdateRequest;
import com.example.petnow.service.PetService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/pet")
@RequiredArgsConstructor
public class PetController {
    private final PetService petService;

    @PostMapping("/create")
    public String addPet(@RequestBody PetCreateRequest createRequest){
        petService.createPet(1L, createRequest);
        return "mypage";
    }

    @PostMapping("/update")
    public String updatePet(@RequestBody PetUpdateRequest updateRequest){
        petService.updatePet(3L,updateRequest);
        return "mypage";
    }
}
