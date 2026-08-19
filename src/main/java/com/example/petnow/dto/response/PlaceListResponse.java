package com.example.petnow.dto.response;

import com.example.petnow.entity.PlaceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PlaceListResponse {

    private Long id;
    private String nickname;
    private String name;
    private String description;
    private PlaceType placeType;
    private boolean allowsSmallDog;
    private boolean allowsMediumDog;
    private boolean allowsLargeDog;
    private boolean providesHomeCamera;
    private boolean providesRealtimePhoto;
    private boolean providesYard;
    private boolean providesWalk;
    private BigDecimal hourlyPrice;

    /**
     * 지도에 찍을 위도. {@code place_addresses} 에 주소 행이 없거나 아직 지오코딩되지 않았으면 {@code null} 이다. (#277)
     *
     * <p>null 인 장소는 지도에서 빠지고 목록에는 그대로 남는다. 화면이 이 값을 필수로 다루면
     * 좌표 하나 없는 장소 때문에 목록 전체가 비게 된다.
     */
    private BigDecimal latitude;

    /**
     * 지도에 찍을 경도. 위도와 짝이며 한쪽만 채워진 행은 좌표가 없는 것으로 본다.
     */
    private BigDecimal longitude;
}
