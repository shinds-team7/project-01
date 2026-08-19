package com.example.petnow.mapper;

import com.example.petnow.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AuthMapper {
    void signup(User user);

    /**
     * 가입 전에 같은 이메일을 쓰는 계정이 있는지 본다.
     *
     * <p>탈퇴한 회원({@code deleted_at IS NOT NULL})도 센다. UNIQUE 제약이 탈퇴 여부를
     * 가리지 않으므로, 여기서 빼면 사전 검사는 통과시켜 놓고 INSERT 가 제약에 걸린다.
     */
    boolean existsByEmail(String email);

    User findByEmail(String email);

    // 마이페이지에서 바로 보여지는 내정보 처리 (상세정보X)
    User findById(Long userId);

    User findByProviderAndProviderId(
        @Param("provider") String provider,
        @Param("providerId") String providerId
    );

    void signupKakao(User user);
}
