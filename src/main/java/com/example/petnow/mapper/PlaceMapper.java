package com.example.petnow.mapper;

import com.example.petnow.entity.Place;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PlaceMapper {
    int insert(Place place);

}