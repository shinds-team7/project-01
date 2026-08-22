package com.example.petnow.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class PlacePhotoResponse {

    private final Long id;
    private final String imageUrl;
    private final Integer sortOrder;
}
