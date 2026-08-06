package com.example.petnow.controller;

import com.example.petnow.common.constant.SessionConst;
import com.example.petnow.dto.response.PetListResponse;
import com.example.petnow.dto.response.UserMyPageResponse;
import com.example.petnow.entity.Pet;
import com.example.petnow.service.PetService;
import com.example.petnow.service.UserService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 마이페이지 라우팅 회귀 테스트.
 *
 * <p>이 화면은 매핑이 두 컨트롤러에 흩어져 있었고, 등록·수정·로그인 POST 가
 * 리다이렉트 없이 뷰 이름 "mypage" 를 반환하고 있었습니다. 그 두 가지가
 * 다시 생기지 않도록 고정합니다.
 */
@WebMvcTest({MyPageController.class, PetController.class, UserController.class})
class MyPageRoutingTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    PetService petService;

    @MockitoBean
    UserService userService;

    private MockHttpSession loggedIn() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SessionConst.LOGIN_USER_ID, 1L);
        return session;
    }

    @Nested
    @DisplayName("GET /mypage")
    class Render {

        @Test
        @DisplayName("반려동물 목록이 실제 데이터로 그려지고 상세 링크가 붙는다")
        void rendersPetList() throws Exception {
            given(petService.getPetList(1L)).willReturn(List.of(
                    PetListResponse.builder().id(7L).name("초코").size(Pet.Size.SMALL).weight(4.2).build(),
                    PetListResponse.builder().id(8L).name("보리").size(Pet.Size.LARGE).weight(11.0).build()
            ));
            given(userService.getMyPage(1L)).willReturn(
                    UserMyPageResponse.builder().nickname("지우").email("a@b.c").build());

            mockMvc.perform(get("/mypage").session(loggedIn()))
                    .andExpect(status().isOk())
                    .andExpect(view().name("mypage"))
                    .andExpect(content().string(Matchers.containsString("/pet/detail/7")))
                    .andExpect(content().string(Matchers.containsString("/pet/detail/8")))
                    .andExpect(content().string(Matchers.containsString("지우님")))
                    .andExpect(content().string(Matchers.containsString("소형")));
        }

        @Test
        @DisplayName("등록한 반려동물이 없으면 빈 상태를 보여준다")
        void rendersEmptyState() throws Exception {
            given(petService.getPetList(1L)).willReturn(List.of());
            given(userService.getMyPage(1L)).willReturn(
                    UserMyPageResponse.builder().nickname("지우").email("a@b.c").build());

            mockMvc.perform(get("/mypage").session(loggedIn()))
                    .andExpect(status().isOk())
                    .andExpect(content().string(Matchers.containsString("아직 등록한")));
        }

        @Test
        @DisplayName("비로그인이면 홈으로 보낸다 (예전에는 NPE 로 500)")
        void redirectsAnonymous() throws Exception {
            mockMvc.perform(get("/mypage"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/"));
        }
    }

    @Nested
    @DisplayName("POST 후에는 뷰를 직접 반환하지 않고 리다이렉트한다")
    class PostRedirect {

        @Test
        @DisplayName("반려동물 등록 → /mypage")
        void createPet() throws Exception {
            mockMvc.perform(post("/pet/create").session(loggedIn())
                            .param("name", "초코")
                            .param("size", "SMALL")
                            .param("weight", "4.2"))
                    .andExpect(redirectedUrl("/mypage"));
        }

        @Test
        @DisplayName("반려동물 수정 → /mypage")
        void updatePet() throws Exception {
            mockMvc.perform(post("/pet/update").session(loggedIn())
                            .param("petId", "7")
                            .param("name", "초코"))
                    .andExpect(redirectedUrl("/mypage"));
        }

        @Test
        @DisplayName("로그인 → /mypage")
        void login() throws Exception {
            given(userService.login(ArgumentMatchers.any())).willReturn(1L);

            mockMvc.perform(post("/user/login")
                            .param("email", "a@b.c")
                            .param("password", "pw1234"))
                    .andExpect(redirectedUrl("/mypage"));
        }

        @Test
        @DisplayName("회원가입은 세션이 없으므로 마이페이지가 아니라 홈으로")
        void signup() throws Exception {
            mockMvc.perform(post("/user/signup")
                            .param("email", "a@b.c")
                            .param("nickname", "지우")
                            .param("password", "pw1234"))
                    .andExpect(redirectedUrl("/"));
        }
    }

    @Test
    @DisplayName("마이페이지의 '아이 등록' 링크(/pet/new)에 매핑이 있다")
    void petCreateFormIsReachable() throws Exception {
        mockMvc.perform(get("/pet/new").session(loggedIn()))
                .andExpect(status().isOk())
                .andExpect(view().name("pet-form"));
    }
}
