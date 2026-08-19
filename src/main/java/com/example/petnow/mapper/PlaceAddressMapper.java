package com.example.petnow.mapper;

import com.example.petnow.entity.PlaceAddress;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface PlaceAddressMapper {

    PlaceAddress findByPlaceId(Long placeId);

    List<PlaceAddress> findWithoutCoordinates(@Param("limit") int limit);

    int updateCoordinates(@Param("placeId") Long placeId,
                          @Param("latitude") BigDecimal latitude,
                          @Param("longitude") BigDecimal longitude);
}
