package com.example.petnow.controller;

import com.example.petnow.common.constant.SessionConst;
import com.example.petnow.dto.response.PetListResponse;
import com.example.petnow.dto.response.UserMyPageResponse;
import com.example.petnow.entity.Pet;
import com.example.petnow.service.AuthService;
import com.example.petnow.service.PetService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
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

    @MockBean
    PetService petService;

    @MockBean
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
                .andExpect(content().string(org.hamcrest.Matchers.containsString("지우님")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("/pet/detail/7")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("소형")));
    }

    @Test
    void rendersEmptyPetState() throws Exception {
        given(petService.getPetList(1L)).willReturn(List.of());
        given(authService.getMyPage(1L)).willReturn(
                UserMyPageResponse.builder().nickname("지우").email("a@b.c").build());

        mockMvc.perform(get("/mypage").session(loggedIn()))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("아직 등록한 반려동물이 없어요")));
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
