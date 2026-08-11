package com.example.petnow.mapper;

import com.example.petnow.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AuthMapper {
    void signup(User user);

    User findByEmail(String email);

    // 마이페이지에서 바로 보여지는 내정보 처리 (상세정보X)
    User findById(Long userId);
}
