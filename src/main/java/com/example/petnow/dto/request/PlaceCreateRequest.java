package com.example.petnow.dto.request;

import com.example.petnow.entity.Place;
import com.example.petnow.entity.PlaceStatus;
import com.example.petnow.entity.PlaceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class PlaceCreateRequest {

    @NotBlank(message = "장소명을 입력해주세요.")
    @Size(max = 100, message = "장소명은 100자 이하로 입력해주세요.")
    private String name;

    @NotBlank(message = "소개글을 필수로 입력해주세요")
    private String description;

    @NotNull(message = "장소 유형을 선택해주세요.")
    private PlaceType placeType;

    @NotNull(message = "장소 면적을 입력해주세요.")
    @Positive(message = "장소 면적은 0보다 커야 합니다.")
    private BigDecimal areaSize;

    @NotNull(message = "최대 마릿수를 입력해주세요.")
    @Positive(message = "최대 마릿수는 1마리 이상이어야 합니다.")
    private Integer capacity;

    private boolean allowsSmallDog;
    private boolean allowsMediumDog;
    private boolean allowsLargeDog;
    private boolean providesHomeCamera;
    private boolean providesRealtimePhoto;
    private boolean providesYard;
    private boolean providesWalk;
    private boolean otherOptionsEnabled;

    private String otherOptions;
    private BigDecimal hourlyPrice;
    private BigDecimal nightlyPrice;

    public Place toEntity(Long userId) {
        return Place.builder()
                .hostUserId(userId)
                .name(name)
                .description(description)
                .placeType(placeType)
                .areaSize(areaSize)
                .capacity(capacity)
                .allowsSmallDog(allowsSmallDog)
                .allowsMediumDog(allowsMediumDog)
                .allowsLargeDog(allowsLargeDog)
                .providesHomeCamera(providesHomeCamera)
                .providesRealtimePhoto(providesRealtimePhoto)
                .providesYard(providesYard)
                .providesWalk(providesWalk)
                .otherOptions(otherOptionsEnabled ? otherOptions : null)
                .hourlyPrice(hourlyPrice)
                .nightlyPrice(nightlyPrice)
                .status(PlaceStatus.PENDING)
                .visible(true)
                .build();
    }
}
