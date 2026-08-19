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

/**
 * @RestControllerAdvice / @ControllerAdvice 담당 범위가 갈렸는지 검증한다.
 *
 * 핵심 회귀: HTML 을 렌더링하는 컨트롤러에서 예외가 나면 JSON 이 아니라 error.html 이 나가야 하고,
 * 본문이 절대 0바이트면 안 된다.
 */
@WebMvcTest(controllers = {
	ExceptionScopeTestControllers.MvcProbeController.class,
	ExceptionScopeTestControllers.RestProbeController.class
})
@Import(ViewErrorController.class)
class ExceptionHandlerScopeTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	@DisplayName("뷰 컨트롤러에서 예외가 나면 HTML 에러 페이지가 나간다")
	void mvcControllerRendersHtmlErrorPage() throws Exception {
		MockHttpServletResponse response = mockMvc.perform(get("/probe/mvc/boom")
				.accept(MediaType.TEXT_HTML))
			.andReturn()
			.getResponse();

		assertThat(response.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
		assertThat(response.getContentType()).startsWith(MediaType.TEXT_HTML_VALUE);
		assertThat(response.getContentAsString())
			.isNotEmpty()
			.contains("<html")
			.contains(CommonErrorCode.INTERNAL_ERROR.getDefaultMessage());
	}

	@Test
	@DisplayName("MvcExceptionHandler 는 assignableTypes 화이트리스트에 없던 뷰 컨트롤러도 받는다")
	void mvcAdviceCoversControllersWithoutExplicitRegistration() throws Exception {
		// 회귀 방지: 예전에는 assignableTypes 에 등록된 5개 컨트롤러만 처리돼
		// 등록을 빠뜨린 컨트롤러는 빈 500 으로 떨어졌다.
		MockHttpServletResponse response = mockMvc.perform(get("/probe/mvc/business")
				.accept(MediaType.TEXT_HTML))
			.andReturn()
			.getResponse();

		assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
		assertThat(response.getContentAsString())
			.isNotEmpty()
			.contains(ExceptionScopeTestControllers.BUSINESS_MESSAGE);
	}

	@Test
	@DisplayName("@RestController 에서 예외가 나면 JSON 이 나간다")
	void restControllerReturnsJson() throws Exception {
		MockHttpServletResponse response = mockMvc.perform(get("/probe/rest/boom")
				.accept(MediaType.APPLICATION_JSON))
			.andReturn()
			.getResponse();

		assertThat(response.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
		assertThat(response.getContentType()).startsWith(MediaType.APPLICATION_JSON_VALUE);
		assertThat(response.getContentAsString())
			.contains(CommonErrorCode.INTERNAL_ERROR.getCode());
	}

	@Test
	@DisplayName("HTML 을 Accept 하는 @RestController 요청도 JSON 으로 답한다 (스코프가 뒤집히지 않음)")
	void restControllerStaysJsonEvenForHtmlAccept() throws Exception {
		MockHttpServletResponse response = mockMvc.perform(get("/probe/rest/boom")
				.accept(MediaType.TEXT_HTML))
			.andReturn()
			.getResponse();

		assertThat(response.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
		assertThat(response.getContentAsString()).isNotEmpty();
		assertThat(response.getContentType()).startsWith(MediaType.APPLICATION_JSON_VALUE);
	}
}
