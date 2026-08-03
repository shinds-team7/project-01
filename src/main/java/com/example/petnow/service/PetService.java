package com.example.petnow.service;

import com.example.petnow.dto.request.PetCreateRequest;

public interface PetService {
    void createPet(Long userId, PetCreateRequest request);

}