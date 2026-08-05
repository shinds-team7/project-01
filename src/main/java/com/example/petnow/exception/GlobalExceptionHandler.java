package com.example.petnow.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.example.petnow.dto.ErrorResponse;

import lombok.extern.slf4j.Slf4j;

/**
 * @RestController가 붙은(JSON 응답) 컨트롤러 전용 예외 처리기.
 * 뷰를 렌더링하는 @Controller는 {@link MvcExceptionHandler}가 담당한다.
 */
@Slf4j
@RestControllerAdvice(annotations = RestController.class)
public class GlobalExceptionHandler {

	@ExceptionHandler(BusinessException.class)
	public ResponseEntity<ErrorResponse> handleBusiness(BusinessException e) {
		ErrorCode ec = e.getErrorCode();
		log.warn("BusinessException: {} {}", ec.getCode(), e.getMessage());
		return ResponseEntity.status(ec.getStatus())
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
			.body(ErrorResponse.builder()
				.code(CommonErrorCode.VALIDATION_ERROR.getCode())
				.message(message)
				.build());
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleException(Exception e) {
		log.error("Unhandled exception", e);
		return ResponseEntity.status(CommonErrorCode.INTERNAL_ERROR.getStatus())
			.body(ErrorResponse.builder()
				.code(CommonErrorCode.INTERNAL_ERROR.getCode())
				.message(CommonErrorCode.INTERNAL_ERROR.getDefaultMessage())
				.build());
	}

}