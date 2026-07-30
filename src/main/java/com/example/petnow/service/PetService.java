package com.example.petnow.service;

import com.example.petnow.dto.request.PetCreateRequest;
import com.example.petnow.dto.request.PetUpdateRequest;
import com.example.petnow.dto.response.PetListResponse;

import java.util.List;

public interface PetService {
    void createPet(Long userId, PetCreateRequest request);
    List<PetListResponse> getPetList(Long userId);
    void updatePet(Long petId, PetUpdateRequest request);
}