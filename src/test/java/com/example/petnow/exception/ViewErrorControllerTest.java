package com.example.petnow.exception;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;

import jakarta.servlet.RequestDispatcher;

/**
 * 핸들러 밖에서 터진 예외(뷰 렌더링 중 예외·404)의 ERROR 디스패치 동작 검증.
 *
 * <p>보고된 회귀 그대로: 응답 Content-Type 이 이미 text/html 로 preset 된 상태에서
 * /error 로 forward 되어도 본문 0바이트 500 이 나가면 안 된다.
 */
@WebMvcTest(controllers = ViewErrorController.class)
@Import(ViewErrorController.class)
class ViewErrorControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	@DisplayName("렌더링 중 예외로 text/html 이 preset 된 채 /error 로 와도 HTML 본문이 나간다")
	void rendersHtmlWhenContentTypeAlreadyPresetToHtml() throws Exception {
		MockHttpServletResponse response = mockMvc.perform(get("/error")
				.requestAttr(RequestDispatcher.ERROR_STATUS_CODE, 500)
				.requestAttr(RequestDispatcher.ERROR_REQUEST_URI, "/places/1")
				.requestAttr(RequestDispatcher.ERROR_EXCEPTION,
					new IllegalStateException("템플릿 렌더링 중 예외"))
				.contentType(MediaType.TEXT_HTML)
				.accept(MediaType.ALL))
			.andReturn()
			.getResponse();

		assertThat(response.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
		assertThat(response.getContentType()).startsWith(MediaType.TEXT_HTML_VALUE);
		// 핵심 회귀: 0바이트면 안 된다.
		assertThat(response.getContentAsByteArray()).isNotEmpty();
		assertThat(response.getContentAsString())
			.contains("<html")
			.contains(CommonErrorCode.INTERNAL_ERROR.getDefaultMessage());
	}

	@Test
	@DisplayName("/api/** 요청의 ERROR 디스패치는 JSON 으로 답한다")
	void rendersJsonForApiRequests() throws Exception {
		MockHttpServletResponse response = mockMvc.perform(get("/error")
				.requestAttr(RequestDispatcher.ERROR_STATUS_CODE, 500)
				.requestAttr(RequestDispatcher.ERROR_REQUEST_URI, "/api/frontend/capabilities")
				.accept(MediaType.ALL))
			.andReturn()
			.getResponse();

		assertThat(response.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
		assertThat(response.getContentType()).startsWith(MediaType.APPLICATION_JSON_VALUE);
		assertThat(response.getContentAsString())
			.contains(CommonErrorCode.INTERNAL_ERROR.getCode());
	}

	@Test
	@DisplayName("404 는 5xx 메시지를 노출하지 않고 상태 그대로 HTML 로 나간다")
	void rendersNotFoundAsHtml() throws Exception {
		MockHttpServletResponse response = mockMvc.perform(get("/error")
				.requestAttr(RequestDispatcher.ERROR_STATUS_CODE, 404)
				.requestAttr(RequestDispatcher.ERROR_REQUEST_URI, "/places/does-not-exist")
				.accept(MediaType.TEXT_HTML))
			.andReturn()
			.getResponse();

		assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
		assertThat(response.getContentAsString())
			.isNotEmpty()
			.doesNotContain(CommonErrorCode.INTERNAL_ERROR.getDefaultMessage());
	}
}
