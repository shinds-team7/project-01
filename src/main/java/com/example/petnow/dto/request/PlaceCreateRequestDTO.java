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

@Data
@NoArgsConstructor
public class PlaceCreateRequestDTO {

    @NotBlank(message = "장소명을 입력해주세요.")
    @Size(max = 100, message = "장소명은 100자 이하로 입력해주세요.")
    private String title;

    @NotBlank(message = "주소를 입력해주세요.")
    private String address;

    private String detailAddress;

    @NotBlank(message = "소개글을 필수로 입력해주세요")
    private String info;

    @NotNull(message = "장소 유형을 선택해주세요.")
    private PlaceType placeType;

    @NotNull(message = "장소 면적을 입력해주세요.")
    @Positive(message = "장소 면적은 0보다 커야 합니다.")
    private Integer placeArea;

    @NotNull(message = "최대 마릿수를 입력해주세요.")
    @Positive(message = "최소 마릿수는 1마리 이상이어야 합니다.")
    private Integer maxDogCount;

    private boolean optionSmallDog;
    private boolean optionMediumDog;
    private boolean optionLargeDog;
    private boolean optionHomeCam;
    private boolean optionRealTimePhoto;
    private boolean optionYard;
    private boolean optionWalkService;
    private boolean optionEtc;

    private String optionText;
    private Long hourPrice;
    private Long dayPrice;

    @NotNull(message = "게시 상태를 선택해주세요.")
    private PlaceStatus placeStatus;

    public Place toEntity(Long userId) {
        return Place.builder()
                .userId(userId)
                .title(title)
                .address(address)
                .detailAddress(detailAddress)
                .info(info)
                .placeType(placeType)
                .placeArea(placeArea)
                .maxDogCount(maxDogCount)
                .optionSmallDog(optionSmallDog)
                .optionMediumDog(optionMediumDog)
                .optionLargeDog(optionLargeDog)
                .optionHomeCam(optionHomeCam)
                .optionRealTimePhoto(optionRealTimePhoto)
                .optionYard(optionYard)
                .optionWalkService(optionWalkService)
                .optionEtc(optionEtc)
                .optionText(optionEtc ? optionText : null)
                .hourPrice(hourPrice)
                .dayPrice(dayPrice)
                .placeStatus(placeStatus)
                .build();
    }
}
