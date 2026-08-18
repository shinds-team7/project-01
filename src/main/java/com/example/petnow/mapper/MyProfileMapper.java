package com.example.petnow.mapper;

import com.example.petnow.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface MyProfileMapper {

    User findById(@Param("userId") Long userId);

    boolean existsActiveByNicknameExcludingUser(@Param("userId") Long userId,
                                                @Param("nickname") String nickname);

    int updateProfile(@Param("userId") Long userId,
                      @Param("nickname") String nickname,
                      @Param("phone") String phone,
                      @Param("profileImageUrl") String profileImageUrl);

    int updatePassword(@Param("userId") Long userId,
                       @Param("password") String password);

    int withdraw(@Param("userId") Long userId);
}
