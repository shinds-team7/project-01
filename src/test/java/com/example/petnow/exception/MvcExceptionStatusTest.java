package com.example.petnow.exception;

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
	@DisplayName("서비스가 던진 ResponseStatusException(403)은 403으로 나간다")
	void responseStatusException_keepsItsOwnStatus() throws Exception {
		when(placeService.getPlaceDetail(anyLong(), any()))
			.thenThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "권한 없음"));

		mockMvc.perform(get("/places/1"))
			.andExpect(status().isForbidden())
			.andExpect(view().name("error"));
	}

	@Test
	@DisplayName("PathVariable 타입 변환 실패는 400으로 나간다")
	void typeMismatch_is400() throws Exception {
		// GET /pet/detail/{petId} 의 petId 는 Long 이라 "abc" 는 변환에 실패한다.
		// MethodArgumentTypeMismatchException 은 자기 상태(400)를 아는 ErrorResponse 다.
		mockMvc.perform(get("/pet/detail/abc"))
			.andExpect(status().isBadRequest())
			.andExpect(view().name("error"));
	}

	@Test
	@DisplayName("지원하지 않는 HTTP 메서드는 405로 나간다")
	void methodNotSupported_is405() throws Exception {
		mockMvc.perform(post("/places/1"))
			.andExpect(status().isMethodNotAllowed());
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
		// MaxUploadSizeExceededException 은 스스로 413 을 알고 있어 fallback 에 맡겨도 상태 코드는 맞다.
		// 다만 fallback 은 4xx 에서 영어 reason phrase("Content Too Large")를 내보내므로 전용 핸들러가 필요하다.
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
}
