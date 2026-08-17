package com.example.petnow;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

// local 프로파일은 .gitignore 대상인 application-local.yaml 에 의존하므로
// 클린 클론과 CI 에서는 존재하지 않는다. 테스트는 항상 test 프로파일로 띄운다.
@SpringBootTest
@ActiveProfiles("test")
class PetNowApplicationTests {

    @Test
    void contextLoads() {
    }

}
