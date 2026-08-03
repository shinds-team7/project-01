package com.example.petnow.controller;

import com.example.petnow.dto.request.PetCreateRequest;
import com.example.petnow.service.PetService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.Mapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
@RequiredArgsConstructor
public class PetController {
    private final PetService petService;

    @PostMapping("/addpet")
    public String addpet(@RequestBody PetCreateRequest request){

        petService.createPet(1L,request);
        return "mypage";
    }
}
