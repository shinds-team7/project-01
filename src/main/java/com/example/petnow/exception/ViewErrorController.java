package com.example.petnow.exception;

import org.springframework.boot.webmvc.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.example.petnow.dto.response.ErrorResponse;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;

/**
 * 컨테이너 ERROR 디스패치 전담 컨트롤러. Spring Boot 기본 {@code BasicErrorController} 를 대체한다.
 *
 * <p>{@code @ExceptionHandler} 는 핸들러 메서드 실행 중에 터진 예외만 잡는다. 뷰 렌더링이
 * 시작된 뒤(Thymeleaf 템플릿 평가 중) 터진 예외나 404 는 {@link MvcExceptionHandler} 에
 * 도달하지 못하고 서블릿 컨테이너로 빠져나가 {@code /error} 로 forward 된다.
 *
 * <p>기본 {@code BasicErrorController} 는 이 지점에서 두 개의 매핑
 * ({@code produces=text/html} 인 것과 {@code Map} 을 JSON 으로 쓰는 것)을 Accept 헤더로 골라내는데,
 * 렌더링 도중 예외라면 응답의 Content-Type 이 이미 {@code text/html} 로 preset 된 상태다.
 * 여기서 {@code Map} 을 쓰는 쪽이 선택되면
 * {@code HttpMessageNotWritableException: No converter for [class java.util.LinkedHashMap]
 * with preset Content-Type 'text/html;charset=UTF-8'} 이 터지고 본문 0바이트 500 이 나간다.
 *
 * <p>그래서 매핑을 하나만 두고 협상 없이 경로로 판정한다. {@code /api/**} 는 JSON,
 * 나머지는 {@code error.html}. 어느 쪽이든 Content-Type 을 명시적으로 덮어써서
 * preset 된 값이 컨버터 선택을 망치지 않게 한다.
 */
@Slf4j
@Controller
@RequestMapping(ViewErrorController.ERROR_PATH)
public class ViewErrorController implements ErrorController {

	static final String ERROR_PATH = "/error";

	private static final String API_PREFIX = "/api/";

	@RequestMapping
	public Object handleError(HttpServletRequest request, HttpServletResponse response) {
		HttpStatus status = resolveStatus(request);
		String requestUri = attribute(request, RequestDispatcher.ERROR_REQUEST_URI);

		Object exception = request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);
		if (exception instanceof Throwable t) {
			log.error("Unhandled exception outside handler: {} {}", requestUri, status.value(), t);
		} else {
			log.warn("Error dispatch: {} {}", requestUri, status.value());
		}

		// 실패한 렌더링이 남긴 부분 출력물을 버린다. 이미 커밋됐다면 손댈 수 없다.
		if (!response.isCommitted()) {
			response.resetBuffer();
		}

		return prefersJson(requestUri)
			? jsonResponse(status)
			: errorView(status);
	}

	private boolean prefersJson(String requestUri) {
		return requestUri != null && requestUri.startsWith(API_PREFIX);
	}

	private ResponseEntity<ErrorResponse> jsonResponse(HttpStatus status) {
		return ResponseEntity.status(status)
			.contentType(MediaType.APPLICATION_JSON)
			.body(ErrorResponse.builder()
				.code(codeOf(status))
				.message(messageOf(status))
				.build());
	}

	private ModelAndView errorView(HttpStatus status) {
		ModelAndView mav = new ModelAndView("error");
		mav.setStatus(status);
		mav.addObject("status", status.value());
		mav.addObject("message", messageOf(status));
		return mav;
	}

	private HttpStatus resolveStatus(HttpServletRequest request) {
		Object code = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
		if (code instanceof Integer value) {
			HttpStatus resolved = HttpStatus.resolve(value);
			if (resolved != null) {
				return resolved;
			}
		}
		return HttpStatus.INTERNAL_SERVER_ERROR;
	}

	/** 5xx 는 내부 사정을 노출하지 않는다. 4xx 는 상태 이름을 그대로 코드로 쓴다. */
	private String codeOf(HttpStatus status) {
		return status.is5xxServerError()
			? CommonErrorCode.INTERNAL_ERROR.getCode()
			: status.name();
	}

	/**
	 * 4xx 문구는 {@link MvcExceptionHandler} 와 같은 표({@link ClientErrorMessages})를 쓴다.
	 *
	 * <p>404 는 대부분 이쪽으로 들어온다. 여기서만 {@code getReasonPhrase()} 를 쓰면 주소를 잘못 친
	 * 사용자에게 "Not Found" 라는 영어가 그대로 나가서, 핸들러 쪽만 고쳐 봐야 구멍이 남는다.
	 */
	private String messageOf(HttpStatus status) {
		return status.is5xxServerError()
			? CommonErrorCode.INTERNAL_ERROR.getDefaultMessage()
			: ClientErrorMessages.of(status);
	}

	private String attribute(HttpServletRequest request, String name) {
		Object value = request.getAttribute(name);
		return value instanceof String s ? s : null;
	}
}
