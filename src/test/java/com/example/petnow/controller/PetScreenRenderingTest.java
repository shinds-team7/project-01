package com.example.petnow.controller;

import com.example.petnow.common.constant.SessionConst;
import com.example.petnow.dto.request.PetUpdateRequest;
import com.example.petnow.dto.response.PetDetailResponse;
import com.example.petnow.entity.Pet;
import com.example.petnow.service.PetService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/**
 * 반려동물 화면이 프로토타입 앱 셸로 실제로 그려지는지, 폼이 값을 온전히 되돌려 보내는지 확인한다.
 *
 * <p>프로토타입 디자인 시스템(app.css + fragments/base)은 PR #110 이 머지 없이 닫혀 develop 에
 * 들어오지 못했다. 다시 빠지지 않도록 셸 적용 여부까지 함께 못 박는다.
 */
@WebMvcTest(PetController.class)
class PetScreenRenderingTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PetService petService;

    // ───────────────────── 등록 화면 ─────────────────────

    @Test
    @DisplayName("GET /pet/new 이 등록 폼을 앱 셸로 그린다")
    void createFormRendersWithAppShell() throws Exception {
        mockMvc.perform(get("/pet/new").session(loggedIn()))
                .andExpect(status().isOk())
                .andExpect(view().name("pet-form"))
                // fragments/base 가 실제로 적용됐는지 (셸이 빠지면 app.css 링크가 없다)
                .andExpect(content().string(containsString("/css/app.css")))
                .andExpect(content().string(containsString("새 가족을")));
    }

    /**
     * 하한이 2000 으로 박혀 있으면 해가 갈수록 고를 수 있는 나이가 줄어든다.
     * 공통 프래그먼트가 현재 연도 기준 상대 범위를 내려주는지 확인한다.
     */
    @Test
    @DisplayName("출생 연도 선택지가 특정 연도에 박히지 않고 현재 연도 기준 30년 범위로 나온다")
    void birthYearRangeIsRelativeToThisYear() throws Exception {
        int thisYear = java.time.Year.now().getValue();

        mockMvc.perform(get("/pet/new").session(loggedIn()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("value=\"" + thisYear + "\"")))
                .andExpect(content().string(containsString("value=\"" + (thisYear - 30) + "\"")))
                .andExpect(content().string(org.hamcrest.Matchers.not(
                        containsString("value=\"" + (thisYear - 31) + "\""))));
    }

    @Test
    @DisplayName("비로그인 상태로 등록 폼에 가면 로그인 화면으로 보낸다")
    void createFormRequiresLogin() throws Exception {
        mockMvc.perform(get("/pet/new"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/auth/login"));
    }

    @Test
    @DisplayName("등록 후에는 뷰를 직접 반환하지 않고 마이페이지로 리다이렉트한다")
    void createRedirectsToMyPage() throws Exception {
        mockMvc.perform(post("/pet/create").session(loggedIn()).param("name", "초코"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/mypage"));
    }

    // ───────────────────── 수정 화면 ─────────────────────

    @Test
    @DisplayName("수정 화면이 앱 셸로 그려지고 hidden petId 를 되돌려 보낸다")
    void updateFormRendersWithPetId() throws Exception {
        given(petService.getDetail(1L, 7L)).willReturn(detail());

        mockMvc.perform(get("/pet/detail/7").session(loggedIn()))
                .andExpect(status().isOk())
                .andExpect(view().name("mypage/petUpdate"))
                .andExpect(content().string(containsString("/css/app.css")))
                .andExpect(content().string(containsString("name=\"petId\" value=\"7\"")))
                // 공통 프래그먼트로 뺀 연도 선택지가 기존 값을 선택된 상태로 되돌려야 한다
                .andExpect(content().string(containsString("value=\"2021\" selected")))
                // 삭제는 확인 화면을 거친다
                .andExpect(content().string(containsString("/pet/delete/7")));
    }

    @Test
    @DisplayName("수정 폼이 메모·성별·출생연도까지 바인딩한다")
    void updateBindsEveryEditableField() throws Exception {
        mockMvc.perform(post("/pet/update").session(loggedIn())
                        .param("petId", "7")
                        .param("name", "초코")
                        .param("size", "SMALL")
                        .param("weight", "4.2")
                        .param("birthYear", "2021")
                        .param("sex", "M")
                        .param("isNeutered", "true")
                        .param("memo", "낯선 사람을 조금 무서워해요"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/mypage"));

        ArgumentCaptor<PetUpdateRequest> captor = ArgumentCaptor.forClass(PetUpdateRequest.class);
        verify(petService).updatePet(eq(1L), captor.capture());

        PetUpdateRequest sent = captor.getValue();
        // 폼 필드명이 DTO 와 어긋나면 조용히 null 로 덮어써진다
        assertThat(sent.getMemo()).isEqualTo("낯선 사람을 조금 무서워해요");
        assertThat(sent.getSex()).isEqualTo("M");
        assertThat(sent.getBirthYear()).isEqualTo(2021);
        assertThat(sent.getIsNeutered()).isTrue();
    }

    @Test
    @DisplayName("중성화 체크를 풀면 필드 마커 덕분에 true 로 남지 않고 false 로 내려간다")
    void uncheckedNeuteredBindsFalse() throws Exception {
        mockMvc.perform(post("/pet/update").session(loggedIn())
                        .param("petId", "7")
                        .param("name", "초코")
                        // 체크를 풀면 브라우저는 isNeutered 를 보내지 않고 _isNeutered 만 보낸다
                        .param("_isNeutered", "on"))
                .andExpect(status().is3xxRedirection());

        ArgumentCaptor<PetUpdateRequest> captor = ArgumentCaptor.forClass(PetUpdateRequest.class);
        verify(petService).updatePet(eq(1L), captor.capture());

        assertThat(captor.getValue().getIsNeutered()).isFalse();
    }

    // ───────────────────── 삭제 확인 화면 ─────────────────────

    /**
     * 실제 삭제(POST)의 인증·소유자 검증은 PetDeleteAuthorizationTest 가 맡는다.
     * 여기서는 이번에 새로 생긴 GET 화면들이 세션 없이 열리지 않는지만 본다.
     */
    @Test
    @DisplayName("비로그인 상태로는 수정·삭제 화면이 열리지 않고 서비스도 건드리지 않는다")
    void petScreensRequireLogin() throws Exception {
        for (String path : new String[]{"/pet/detail/7", "/pet/delete/7"}) {
            mockMvc.perform(get(path))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/auth/login"));
        }

        verify(petService, never()).getDetail(any(), any());
    }

    @Test
    @DisplayName("비로그인 상태의 등록·수정 전송은 서비스를 건드리지 않는다")
    void petMutationsRequireLogin() throws Exception {
        mockMvc.perform(post("/pet/create").param("name", "초코"))
                .andExpect(redirectedUrl("/auth/login"));
        mockMvc.perform(post("/pet/update").param("petId", "7"))
                .andExpect(redirectedUrl("/auth/login"));

        verify(petService, never()).createPet(any(), any());
        verify(petService, never()).updatePet(any(), any());
    }

    @Test
    @DisplayName("GET /pet/delete/{petId} 가 삭제 확인 화면을 그린다")
    void deleteFormRenders() throws Exception {
        given(petService.getDetail(1L, 7L)).willReturn(detail());

        mockMvc.perform(get("/pet/delete/7").session(loggedIn()))
                .andExpect(status().isOk())
                .andExpect(view().name("mypage/petDelete"))
                .andExpect(content().string(containsString("/css/app.css")))
                .andExpect(content().string(containsString("삭제할까요")))
                // 실제 삭제는 소유자 검증이 붙은 POST /pet/delete/{petId} 로 간다
                .andExpect(content().string(containsString("action=\"/pet/delete/7\"")));
    }

    private PetDetailResponse detail() {
        return PetDetailResponse.builder()
                .name("초코")
                .size(Pet.Size.SMALL)
                .weight(4.2)
                .birthYear(2021)
                .sex("M")
                .isNeutered(true)
                .memo("낯선 사람을 조금 무서워해요")
                .build();
    }

    private MockHttpSession loggedIn() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SessionConst.LOGIN_USER_ID, 1L);
        return session;
    }
}
