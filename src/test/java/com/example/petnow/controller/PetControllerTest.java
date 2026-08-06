package com.example.petnow.controller;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.petnow.common.constant.SessionConst;
import com.example.petnow.dto.request.PetCreateRequest;
import com.example.petnow.dto.request.PetUpdateRequest;
import com.example.petnow.dto.response.PetDetailResponse;
import com.example.petnow.entity.Pet;
import com.example.petnow.service.PetService;
import com.example.petnow.support.RenderDump;

@WebMvcTest(PetController.class)
class PetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PetService petService;

    @Test
    @DisplayName("로그인한 사용자는 반려동물 등록 화면을 본다")
    void createForm() throws Exception {
        mockMvc.perform(get("/pet/new")
                        .sessionAttr(SessionConst.LOGIN_USER_ID, 1L))
                .andExpect(status().isOk())
                .andExpect(view().name("pet-form"))
                .andExpect(content().string(containsString("새 가족을")))
                .andExpect(content().string(containsString("name=\"isNeutered\"")))
                .andExpect(content().string(not(containsString("class=\"app-nav\""))))
                .andDo(RenderDump.to("pet-create"));
    }

    @Test
    @DisplayName("반려동물 상세 정보는 수정 화면에 렌더링된다")
    void updateForm() throws Exception {
        PetDetailResponse pet = PetDetailResponse.builder()
                .name("초코")
                .size(Pet.Size.SMALL)
                .weight(4.2)
                .birthYear(2023)
                .sex("F")
                .isNeutered(true)
                .memo("산책을 좋아해요")
                .build();
        when(petService.getDetail(1L, 7L)).thenReturn(pet);

        mockMvc.perform(get("/pet/detail/7")
                        .sessionAttr(SessionConst.LOGIN_USER_ID, 1L))
                .andExpect(status().isOk())
                .andExpect(view().name("mypage/petUpdate"))
                .andExpect(content().string(containsString("초코")))
                .andExpect(content().string(containsString("value=\"7\"")))
                .andExpect(content().string(containsString("변경사항 저장하기")))
                .andDo(RenderDump.to("pet-update"));
    }

    @Test
    @DisplayName("등록과 수정은 처리 후 마이페이지로 리다이렉트한다")
    void submitForms() throws Exception {
        mockMvc.perform(post("/pet/create")
                        .sessionAttr(SessionConst.LOGIN_USER_ID, 1L)
                        .param("name", "초코")
                        .param("size", "SMALL")
                        .param("isNeutered", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/mypage"));

        mockMvc.perform(post("/pet/update")
                        .sessionAttr(SessionConst.LOGIN_USER_ID, 1L)
                        .param("petId", "7")
                        .param("name", "초코")
                        .param("birthYear", "2023")
                        .param("sex", "F")
                        .param("size", "SMALL")
                        .param("weight", "4.2")
                        .param("isNeutered", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/mypage"));

        verify(petService).createPet(org.mockito.ArgumentMatchers.eq(1L), any(PetCreateRequest.class));
        verify(petService).updatePet(org.mockito.ArgumentMatchers.eq(1L), any(PetUpdateRequest.class));
    }

    @Test
    @DisplayName("삭제 확인 화면은 대상 정보를 보여주고 삭제 후 마이페이지로 돌아간다")
    void deletePet() throws Exception {
        PetDetailResponse pet = PetDetailResponse.builder()
                .name("초코")
                .size(Pet.Size.SMALL)
                .weight(4.2)
                .build();
        when(petService.getDetail(1L, 7L)).thenReturn(pet);

        mockMvc.perform(get("/pet/delete/7")
                        .sessionAttr(SessionConst.LOGIN_USER_ID, 1L))
                .andExpect(status().isOk())
                .andExpect(view().name("mypage/petDelete"))
                .andExpect(content().string(containsString("삭제할까요?")))
                .andExpect(content().string(containsString("정보 삭제하기")))
                .andExpect(content().string(not(containsString("class=\"app-nav\""))))
                .andDo(RenderDump.to("pet-delete"));

        mockMvc.perform(post("/pet/delete")
                        .sessionAttr(SessionConst.LOGIN_USER_ID, 1L)
                        .param("petId", "7"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/mypage"));

        verify(petService).deletePet(1L, 7L);
    }
}
