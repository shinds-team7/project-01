package com.example.petnow.controller;

import com.example.petnow.common.constant.SessionConst;
import com.example.petnow.service.PetService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(PetController.class)
class PetRoutingPrgTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    PetService petService;

    @Test
    void registrationFormRequiresLogin() throws Exception {
        mockMvc.perform(get("/pet/new"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/auth/login"));
    }

    @Test
    void registrationFormIsReachableWhenLoggedIn() throws Exception {
        mockMvc.perform(get("/pet/new").session(loggedIn()))
                .andExpect(status().isOk())
                .andExpect(view().name("pet-form"));
    }

    @Test
    void createRedirectsToMypage() throws Exception {
        mockMvc.perform(post("/pet/create").session(loggedIn())
                        .param("name", "초코")
                        .param("size", "SMALL")
                        .param("weight", "4.2"))
                .andExpect(redirectedUrl("/mypage"));
    }

    @Test
    void updateRedirectsToMypage() throws Exception {
        mockMvc.perform(post("/pet/update").session(loggedIn())
                        .param("petId", "7")
                        .param("name", "초코"))
                .andExpect(redirectedUrl("/mypage"));
    }

    @Test
    void deleteRedirectsToMypage() throws Exception {
        mockMvc.perform(post("/pet/delete/7").session(loggedIn()))
                .andExpect(redirectedUrl("/mypage"));
    }

    private MockHttpSession loggedIn() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SessionConst.LOGIN_USER_ID, 1L);
        return session;
    }
}
