package com.example.petnow.mapper;

import com.example.petnow.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MyProfileMapper {
    User findById(Long userId);

    int withdraw(Long userId);
}
