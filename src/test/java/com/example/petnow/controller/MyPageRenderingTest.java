package com.example.petnow.controller;

import com.example.petnow.common.constant.SessionConst;
import com.example.petnow.dto.response.PetListResponse;
import com.example.petnow.dto.response.UserMyPageResponse;
import com.example.petnow.entity.Pet;
import com.example.petnow.service.AuthService;
import com.example.petnow.service.PetService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(MyPageController.class)
class MyPageRenderingTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    PetService petService;

    @MockitoBean
    AuthService authService;

    @Test
    void rendersRealUserAndPetData() throws Exception {
        MockHttpSession session = loggedIn();
        given(petService.getPetList(1L)).willReturn(List.of(
                PetListResponse.builder().id(7L).name("초코").size(Pet.Size.SMALL).weight(4.2).build(),
                PetListResponse.builder().id(8L).name("보리").size(Pet.Size.LARGE).weight(11.0).build()));
        given(authService.getMyPage(1L)).willReturn(
                UserMyPageResponse.builder().nickname("지우").email("a@b.c").build());

        mockMvc.perform(get("/mypage").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("mypage"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("/css/app.css")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("class=\"my-profile\"")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("class=\"my-pet-card\"")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("class=\"my-pet-add\"")))
                .andExpect(content().string(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.containsString("class=\"empty my-pet-empty\""))))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("지우")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("/pet/detail/7")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("소형")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("예약·결제 내역")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("호스팅 장소 관리하기")));
    }

    @Test
    void rendersEmptyPetState() throws Exception {
        given(petService.getPetList(1L)).willReturn(List.of());
        given(authService.getMyPage(1L)).willReturn(
                UserMyPageResponse.builder().nickname("지우").email("a@b.c").build());

        mockMvc.perform(get("/mypage").session(loggedIn()))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("class=\"empty my-pet-empty\"")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("아직 등록한 반려동물이 없어요")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("첫 반려동물 등록하기")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("href=\"/pet/new\"")))
                .andExpect(content().string(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.containsString("class=\"my-pet-grid\""))))
                .andExpect(content().string(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.containsString("class=\"my-pet-add\""))));
    }

    /**
     * 내 정보 수정·비밀번호 변경 화면은 /my/profile 을 거쳐야만 닿는다.
     * 마이페이지에 이 링크가 없으면 해당 기능 전체가 화면에서 사라지므로 여기서 고정한다.
     */
    @Test
    void linksToProfileScreen() throws Exception {
        given(petService.getPetList(1L)).willReturn(List.of());
        given(authService.getMyPage(1L)).willReturn(
                UserMyPageResponse.builder().nickname("지우").email("a@b.c").build());

        mockMvc.perform(get("/mypage").session(loggedIn()))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("href=\"/my/profile\"")));
    }

    @Test
    void redirectsAnonymousUsersToLogin() throws Exception {
        mockMvc.perform(get("/mypage"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/auth/login"));
    }

    private MockHttpSession loggedIn() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SessionConst.LOGIN_USER_ID, 1L);
        return session;
    }
}
