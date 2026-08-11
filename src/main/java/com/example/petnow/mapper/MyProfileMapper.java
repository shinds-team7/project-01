package com.example.petnow.mapper;

import com.example.petnow.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MyProfileMapper {

    // 내 정보 상세 조회를 위한 메서드
    User findById(Long userId);

    int withdraw(Long userId);
}
