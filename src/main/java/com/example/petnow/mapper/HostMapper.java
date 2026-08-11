package com.example.petnow.mapper;

import com.example.petnow.dto.response.HostPlaceListResponse;
import com.example.petnow.entity.Place;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface HostMapper {

    int insert(Place place);

    List<HostPlaceListResponse> findAllByUserId(@Param("userId") Long userId);

    /**
     * 호스트 본인의 장소를 소프트 삭제한다.
     *
     * <p>소유자 검증을 WHERE 절에 넣어 남의 장소는 애초에 갱신되지 않게 한다
     * (반려동물 삭제와 같은 방식). 남의 장소든 없는 장소든 0 을 돌려주므로
     * 응답만 보고 존재 여부를 알아낼 수 없다.
     *
     * @return 갱신된 행 수. 0 이면 내 장소가 아니거나 이미 삭제된 것이다.
     */
    int softDelete(@Param("placeId") Long placeId, @Param("hostUserId") Long hostUserId);
}
