package com.example.petnow.controller;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.petnow.common.constant.SessionConst;
import com.example.petnow.dto.response.PetListResponse;
import com.example.petnow.dto.response.UserMyPageResponse;
import com.example.petnow.entity.Pet;
import com.example.petnow.service.PetService;
import com.example.petnow.service.UserService;
import com.example.petnow.support.RenderDump;

@WebMvcTest(MyPageController.class)
class MyPageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PetService petService;

    @MockitoBean
    private UserService userService;

    @Test
    @DisplayName("마이페이지는 보호자와 반려동물 정보를 앱 셸에 렌더링한다")
    void myPage() throws Exception {
        UserMyPageResponse user = UserMyPageResponse.builder()
                .nickname("지우")
                .email("jiwoo@petnow.kr")
                .build();
        PetListResponse pet = PetListResponse.builder()
                .id(7L)
                .name("초코")
                .size(Pet.Size.SMALL)
                .weight(4.2)
                .build();

        when(userService.getMyPage(1L)).thenReturn(user);
        when(petService.getPetList(1L)).thenReturn(List.of(pet));

        mockMvc.perform(get("/mypage")
                        .sessionAttr(SessionConst.LOGIN_USER_ID, 1L))
                .andExpect(status().isOk())
                .andExpect(view().name("mypage/index"))
                .andExpect(content().string(containsString("지우")))
                .andExpect(content().string(containsString("초코")))
                .andExpect(content().string(containsString("CARE PASSPORT")))
                .andExpect(content().string(containsString("aria-current=\"page\"")))
                .andDo(RenderDump.to("mypage-pets"));
    }
}
