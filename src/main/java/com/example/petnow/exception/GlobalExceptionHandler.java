package com.example.petnow.exception;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.example.petnow.dto.response.ErrorResponse;

import lombok.extern.slf4j.Slf4j;

/**
 * {@code @RestController}가 붙은(JSON 응답) 컨트롤러 전용 예외 처리기.
 *
 * <p>스코프를 {@code annotations = RestController.class} 로 명시적으로 좁히고 우선순위를
 * 가장 높게 둔다. 뷰를 렌더링하는 나머지 컨트롤러는 {@link MvcExceptionHandler} 가 fallback 으로
 * 받는다. 이 경계가 무너지면 HTML 요청에 JSON 본문을 쓰려다
 * {@code HttpMessageNotWritableException} 이 터져 본문 0바이트 500 이 나간다.
 *
 * <p>응답마다 Content-Type 을 JSON 으로 명시하는 이유: 예외가 터진 시점에 응답의 Content-Type 이
 * 이미 {@code text/html} 로 preset 되어 있을 수 있고, 그대로 두면 메시지 컨버터를 못 찾는다.
 */
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(annotations = RestController.class)
public class GlobalExceptionHandler {

	@ExceptionHandler(BusinessException.class)
	public ResponseEntity<ErrorResponse> handleBusiness(BusinessException e) {
		ErrorCode ec = e.getErrorCode();
		log.warn("BusinessException: {} {}", ec.getCode(), e.getMessage());
		return ResponseEntity.status(ec.getStatus())
			.contentType(MediaType.APPLICATION_JSON)
			.body(ErrorResponse.builder()
				.code(ec.getCode())
				.message(e.getMessage())
				.build());
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException e) {
		String message = e.getBindingResult().getFieldErrors().stream()
			.findFirst()
			.map(fe -> fe.getField() + " : " + fe.getDefaultMessage())
			.orElse(CommonErrorCode.VALIDATION_ERROR.getDefaultMessage());

		return ResponseEntity.status(CommonErrorCode.VALIDATION_ERROR.getStatus())
			.contentType(MediaType.APPLICATION_JSON)
			.body(ErrorResponse.builder()
				.code(CommonErrorCode.VALIDATION_ERROR.getCode())
				.message(message)
				.build());
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleException(Exception e) {
		log.error("Unhandled exception", e);
		return ResponseEntity.status(CommonErrorCode.INTERNAL_ERROR.getStatus())
			.contentType(MediaType.APPLICATION_JSON)
			.body(ErrorResponse.builder()
				.code(CommonErrorCode.INTERNAL_ERROR.getCode())
				.message(CommonErrorCode.INTERNAL_ERROR.getDefaultMessage())
				.build());
	}

}