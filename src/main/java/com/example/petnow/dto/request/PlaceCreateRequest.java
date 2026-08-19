package com.example.petnow.dto.request;

import com.example.petnow.entity.PlaceType;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
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

    @NotBlank(message = "지역구를 선택해주세요.")
    @Size(max = 50, message = "지역구는 50자 이하로 입력해주세요.")
    private String sigungu;

    @NotBlank(message = "도로명 주소를 입력해주세요.")
    @Size(max = 255, message = "도로명 주소는 255자 이하로 입력해주세요.")
    private String roadAddress;

    @NotNull(message = "장소 유형을 선택해주세요.")
    private PlaceType placeType;

    @NotNull(message = "장소 면적을 입력해주세요.")
    @Positive(message = "장소 면적은 0보다 커야 합니다.")
    @DecimalMax("500.00")
    @Digits(
            integer = 3,
            fraction = 0,
            message = "장소 면적은 소수점 없이 입력해주세요."
    )
    private BigDecimal areaSize;

    @NotNull(message = "최대 마릿수를 입력해주세요.")
    @Positive(message = "최대 마릿수는 1마리 이상이어야 합니다.")
    @Max(value = 20, message = "최대 마릿수는 20마리 이하여야 합니다.")
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

    @NotNull(message = "시간당 요금을 입력해주세요.")
    @Positive(message = "시간당 요금은 0보다 커야 합니다.")
    @DecimalMax(value = "10000000", message = "시간당 요금의 최대값은 1천만원 이하이어야 합니다.")
    @Digits(
            integer = 8,
            fraction = 0,
            message = "시간당 요금은 소수점 없이 입력해주세요."
    )
    private BigDecimal hourlyPrice;

    @NotNull(message = "1박당 요금을 입력해주세요.")
    @Positive(message = "1박당 요금은 0보다 커야 합니다.")
    @DecimalMax(value = "10000000", message = "1박당 요금의 최대값은 1천만원 이하이어야 합니다.")
    @Digits(
            integer = 8,
            fraction = 0,
            message = "1박당 요금은 소수점 없이 입력해주세요."
    )
    private BigDecimal nightlyPrice;

}
