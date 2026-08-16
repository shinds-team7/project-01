package com.example.petnow.exception;

import org.springframework.beans.TypeMismatchException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
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

	/**
	 * 업로드 용량 초과({@code spring.servlet.multipart.max-file-size}).
	 *
	 * <p>이 예외는 {@link ErrorResponse} 를 구현하므로 아래 fallback 에 맡겨도 상태 코드는 413 으로 맞는다.
	 * 다만 fallback 은 4xx 에서 {@code status.getReasonPhrase()} 를 쓰기 때문에 화면에
	 * "Content Too Large" 라는 영어 문구가 그대로 나간다. 5MB 초과는 사용자가 흔히 하는 실수이므로
	 * 무엇이 잘못됐고 얼마까지 되는지 한국어로 알려 준다.
	 */
	@ExceptionHandler(MaxUploadSizeExceededException.class)
	public ModelAndView handleMaxUploadSize(MaxUploadSizeExceededException e) {
		log.warn("업로드 용량 초과: maxUploadSize={}", e.getMaxUploadSize());
		return errorView(ImageErrorCode.IMAGE_TOO_LARGE.getStatus(), ImageErrorCode.IMAGE_TOO_LARGE.getDefaultMessage());
	}

	/**
	 * 마지막 fallback.
	 *
	 * <p>여기서 상태 코드를 무조건 500으로 고정하면 안 된다. 이 핸들러는 {@code Exception} 을 받으므로
	 * Spring MVC 가 던지는 표준 예외({@code MissingServletRequestParameterException},
	 * {@code MethodArgumentTypeMismatchException}, {@code HttpRequestMethodNotSupportedException} 등)와
	 * 서비스 계층의 {@code ResponseStatusException} 까지 전부 걸린다.
	 *
	 * <p>이들은 모두 자기 상태 코드를 알고 있는 {@link ErrorResponse} 구현체다. 그런데
	 * {@code ExceptionHandlerExceptionResolver} 가 {@code ResponseStatusExceptionResolver} 보다
	 * 먼저 동작하기 때문에, 이 핸들러가 먼저 삼켜 버리면 400·403·405 로 나가야 할 응답이
	 * 전부 500 으로 바뀐다. (예: {@code GET /reviews/new?reservationId=abc} 의 타입 변환 실패가
	 * 400 이 아니라 500 으로 뒤바뀐다)
	 *
	 * <p>그래서 예외가 스스로 status 를 갖고 있으면 그 값을 존중하고, 정말 모르는 예외만 500 으로 처리한다.
	 */
	@ExceptionHandler(Exception.class)
	public ModelAndView handleException(Exception e) {
		HttpStatus status = resolveStatus(e);

		if (status.is5xxServerError()) {
			log.error("Unhandled exception", e);
			// 5xx 는 내부 사정을 노출하지 않는다.
			return errorView(status, CommonErrorCode.INTERNAL_ERROR.getDefaultMessage());
		}

		log.warn("{} -> {}", e.getClass().getSimpleName(), status.value());
		return errorView(status, status.getReasonPhrase());
	}

	/**
	 * 예외가 스스로 알고 있는 상태 코드를 꺼낸다. 모르면 500.
	 *
	 * <p>Spring MVC 표준 예외 대부분은 {@link ErrorResponse} 를 구현해 자기 상태를 갖고 있다.
	 * 다만 아래 세 가지는 구현하지 않으면서도 Spring 기본 동작상 400 이므로 따로 맞춰 준다.
	 * <ul>
	 *   <li>{@code TypeMismatchException} — {@code MethodArgumentTypeMismatchException} 의 상위 타입.
	 *       {@code /pet/detail/abc} 처럼 PathVariable 변환이 실패하는 경우</li>
	 *   <li>{@code HttpMessageNotReadableException} — 본문 파싱 실패</li>
	 *   <li>{@code BindException} — 폼 바인딩 실패</li>
	 * </ul>
	 */
	private HttpStatus resolveStatus(Exception e) {
		if (e instanceof ErrorResponse er) {
			HttpStatus resolved = HttpStatus.resolve(er.getStatusCode().value());
			if (resolved != null) {
				return resolved;
			}
		}

		if (e instanceof TypeMismatchException
			|| e instanceof HttpMessageNotReadableException
			|| e instanceof BindException) {
			return HttpStatus.BAD_REQUEST;
		}

		// @ResponseStatus 가 붙은 사용자 정의 예외도 존중한다.
		ResponseStatus annotated = AnnotatedElementUtils.findMergedAnnotation(e.getClass(), ResponseStatus.class);
		if (annotated != null) {
			return annotated.code();
		}

		return CommonErrorCode.INTERNAL_ERROR.getStatus();
	}

	/**
	 * error.html 은 status/message 두 값만 쓴다. 뷰 이름만 반환하면 HTTP 상태가 200 으로
	 * 나가 버리므로(모니터링·검색엔진이 성공으로 인식한다) ModelAndView 에 상태를 실어 보낸다.
	 */
	private ModelAndView errorView(HttpStatus status, String message) {
		ModelAndView mav = new ModelAndView("error");
		mav.setStatus(status);
		mav.addObject("status", status.value());
		mav.addObject("message", message);
		return mav;
	}
}
