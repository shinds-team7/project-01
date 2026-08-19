package com.example.petnow.exception;

import com.example.petnow.common.constant.SessionConst;
import com.example.petnow.controller.PetController;
import com.example.petnow.controller.PlaceController;
import com.example.petnow.service.PetService;
import com.example.petnow.service.PlaceService;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/**
 * MvcExceptionHandler 가 예외의 실제 상태 코드를 응답에 그대로 실어 보내는지 검증한다.
 *
 * <p>이 클래스가 막는 회귀는 두 가지다.
 * <ol>
 *   <li>{@code @ExceptionHandler(Exception.class)} 가 400·403·405 예외를 삼켜 500 으로 바꾸는 것</li>
 *   <li>뷰 이름만 반환해 에러 페이지가 HTTP 200 으로 나가는 것</li>
 * </ol>
 */
@WebMvcTest(controllers = {
	PlaceController.class,
	PetController.class,
	ExceptionScopeTestControllers.MvcProbeController.class
})
@Import(MvcExceptionHandler.class)
class MvcExceptionStatusTest {

	@Autowired
	MockMvc mockMvc;

	@MockitoBean
	PlaceService placeService;

	@MockitoBean
	PetService petService;

	@Test
	@DisplayName("서비스가 던진 ResponseStatusException(403)은 403 과 한국어 문구로 나간다")
	void responseStatusException_keepsItsOwnStatus() throws Exception {
		when(placeService.getPlaceDetail(anyLong(), any()))
			.thenThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "권한 없음"));

		mockMvc.perform(get("/places/1"))
			.andExpect(status().isForbidden())
			.andExpect(view().name("error"))
			// 문구는 예외가 들고 온 reason 이 아니라 상태 코드로 정한다. 사용자에게 보여 줄 문장을
			// 직접 정해야 한다면 ResponseStatusException 이 아니라 BusinessException 을 쓴다.
			.andExpect(model().attribute("message", ClientErrorMessages.of(HttpStatus.FORBIDDEN)));
	}

	@Test
	@DisplayName("PathVariable 타입 변환 실패는 400 과 한국어 문구로 나간다")
	void typeMismatch_is400() throws Exception {
		// GET /pet/detail/{petId} 의 petId 는 Long 이라 "abc" 는 변환에 실패한다.
		// MethodArgumentTypeMismatchException 은 자기 상태(400)를 아는 ErrorResponse 다.
		//
		// 세션을 함께 보내는 이유: /pet/** 는 로그인 인터셉터가 막는 경로다. 인터셉터는 파라미터
		// 바인딩보다 먼저 돌기 때문에, 비로그인으로 부르면 400 에 닿기도 전에 로그인 화면으로 튄다.
		// 여기서 보려는 건 인증이 아니라 타입 변환 실패의 상태 코드다.
		mockMvc.perform(get("/pet/detail/abc").session(loggedInSession()))
			.andExpect(status().isBadRequest())
			.andExpect(view().name("error"))
			// 예전에는 여기에 reason phrase 인 "Bad Request" 가 그대로 나갔다.
			.andExpect(model().attribute("message", CommonErrorCode.VALIDATION_ERROR.getDefaultMessage()));
	}

	@Test
	@DisplayName("지원하지 않는 HTTP 메서드는 405 와 한국어 문구로 나간다")
	void methodNotSupported_is405() throws Exception {
		mockMvc.perform(post("/places/1"))
			.andExpect(status().isMethodNotAllowed())
			// 예전에는 "Method Not Allowed" 가 그대로 나갔다.
			.andExpect(model().attribute("message", ClientErrorMessages.of(HttpStatus.METHOD_NOT_ALLOWED)));
	}

	@Test
	@DisplayName("매핑에 없는 4xx 는 기본 문구로 떨어진다 — 영어가 새어 나가지 않는다")
	void unmappedClientError_usesDefaultMessage() throws Exception {
		// 409 는 ClientErrorMessages 의 표에 없다. reason phrase("Conflict")가 아니라
		// 기본 문구로 나가야 새 상태 코드가 생겨도 영어가 노출되지 않는다.
		when(placeService.getPlaceDetail(anyLong(), any()))
			.thenThrow(new ResponseStatusException(HttpStatus.CONFLICT));

		mockMvc.perform(get("/places/1"))
			.andExpect(status().isConflict())
			.andExpect(view().name("error"))
			.andExpect(model().attribute("message", ClientErrorMessages.DEFAULT_MESSAGE));
	}

	@Test
	@DisplayName("BusinessException 은 ErrorCode 의 상태 코드로 나간다 (200이 아니다)")
	void businessException_usesErrorCodeStatus() throws Exception {
		when(placeService.getPlaceDetail(anyLong(), any()))
			.thenThrow(new com.example.petnow.exception.BusinessException(PlaceErrorCode.PLACE_NOT_FOUND));

		mockMvc.perform(get("/places/999"))
			.andExpect(status().isNotFound())
			.andExpect(view().name("error"));
	}

	@Test
	@DisplayName("업로드 용량 초과는 413 과 한국어 안내 문구로 나간다")
	void maxUploadSizeExceeded_is413WithGuideMessage() throws Exception {
		// MaxUploadSizeExceededException 은 스스로 413 을 알고 있어 fallback 이 그대로 받는다.
		// 전용 @ExceptionHandler 없이 ClientErrorMessages 의 413 매핑만으로 안내 문구가 나가야 한다.
		mockMvc.perform(get("/probe/mvc/upload-too-large"))
			.andExpect(status().is(HttpStatus.CONTENT_TOO_LARGE.value()))
			.andExpect(view().name("error"))
			.andExpect(model().attribute("message", ImageErrorCode.IMAGE_TOO_LARGE.getDefaultMessage()));
	}

	@Test
	@DisplayName("상태 코드를 모르는 예외만 500으로 나간다")
	void unknownException_is500() throws Exception {
		when(placeService.getPlaceDetail(anyLong(), any()))
			.thenThrow(new IllegalStateException("bang"));

		mockMvc.perform(get("/places/1"))
			.andExpect(status().isInternalServerError())
			.andExpect(view().name("error"));
	}

	private MockHttpSession loggedInSession() {
		MockHttpSession session = new MockHttpSession();
		session.setAttribute(SessionConst.LOGIN_USER_ID, 1L);
		return session;
	}
}
