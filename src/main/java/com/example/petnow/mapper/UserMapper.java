package com.example.petnow.mapper;

import com.example.petnow.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper {
    void signup(User user);

    User findByEmail(String email);

    User findById(Long userId);
}
