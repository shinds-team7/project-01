package com.example.petnow.exception;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

import lombok.extern.slf4j.Slf4j;

/**
 * 뷰(HTML)를 렌더링하는 컨트롤러 전용 예외 처리기.
 *
 * <p>예외 처리 담당 범위는 세 곳으로 나뉜다.
 * <ol>
 *   <li>{@link GlobalExceptionHandler} — {@code @RestController} 만. JSON 응답.
 *       우선순위 HIGHEST_PRECEDENCE.</li>
 *   <li>이 클래스 — 나머지 컨트롤러 전부. {@code error.html} 렌더링.
 *       우선순위 LOWEST_PRECEDENCE 이므로 위에서 걸리지 않은 것만 받는다.</li>
 *   <li>{@link ViewErrorController} — 핸들러 밖(뷰 렌더링 중·404 등)에서 터져
 *       {@code @ExceptionHandler} 가 구조적으로 잡을 수 없는 예외.</li>
 * </ol>
 *
 * <p>이 클래스에 {@code annotations}/{@code assignableTypes} 스코프를 걸지 않는 이유:
 * {@code annotations = Controller.class} 로 좁히면 {@code @RestController} 가 {@code @Controller} 를
 * 메타 애노테이션으로 가지므로 REST 컨트롤러까지 걸려 GlobalExceptionHandler 와 충돌한다.
 * 반대로 {@code assignableTypes} 화이트리스트를 두면 새 뷰 컨트롤러를 등록하는 것을 잊는 순간
 * 그 컨트롤러만 조용히 빈 500 응답으로 되돌아간다. 그래서 REST 쪽만 명시적으로 좁히고
 * 이쪽은 우선순위를 가장 낮게 둔 fallback 으로 유지한다.
 */
@Slf4j
@Order(Ordered.LOWEST_PRECEDENCE)
@ControllerAdvice
public class MvcExceptionHandler {

	@ExceptionHandler(BusinessException.class)
	public ModelAndView handleBusiness(BusinessException e) {
		ErrorCode ec = e.getErrorCode();
		log.warn("BusinessException: {} {}", ec.getCode(), e.getMessage());
		return errorView(ec.getStatus(), e.getMessage());
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ModelAndView handleValidation(MethodArgumentNotValidException e) {
		String message = e.getBindingResult().getFieldErrors().stream()
			.findFirst()
			.map(fe -> fe.getField() + " : " + fe.getDefaultMessage())
			.orElse(CommonErrorCode.VALIDATION_ERROR.getDefaultMessage());

		return errorView(CommonErrorCode.VALIDATION_ERROR.getStatus(), message);
	}

	@ExceptionHandler(Exception.class)
	public ModelAndView handleException(Exception e) {
		log.error("Unhandled exception", e);
		return errorView(CommonErrorCode.INTERNAL_ERROR.getStatus(),
			CommonErrorCode.INTERNAL_ERROR.getDefaultMessage());
	}

	/**
	 * error.html 은 status/message 두 값만 쓴다. 뷰 이름만 반환하면 HTTP 상태가 200 으로
	 * 나가므로 ModelAndView 에 상태를 실어 보낸다.
	 */
	private ModelAndView errorView(HttpStatus status, String message) {
		ModelAndView mav = new ModelAndView("error");
		mav.setStatus(status);
		mav.addObject("status", status.value());
		mav.addObject("message", message);
		return mav;
	}
}
