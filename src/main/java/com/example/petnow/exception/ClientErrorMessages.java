package com.example.petnow.exception;

import java.util.Map;

import org.springframework.http.HttpStatus;

/**
 * 4xx 상태 코드를 화면에 띄울 한국어 안내 문구로 바꾼다.
 *
 * <p>{@link MvcExceptionHandler} 의 fallback 은 예외가 스스로 아는 상태 코드를 존중하는 대신
 * 문구를 만들 방법이 없어 {@code HttpStatus.getReasonPhrase()} 를 썼다. 그런데 이 값은 스펙에
 * 정의된 영어 문자열이라 한국어 서비스 화면에 "Bad Request" · "Method Not Allowed" 가 그대로 나갔다.
 *
 * <p>케이스마다 전용 {@code @ExceptionHandler} 를 추가하는 방식으로는 새 예외가 생길 때마다 또
 * 새어 나간다. 그래서 예외가 아니라 <b>상태 코드</b>를 기준으로 문구를 정하고, 그 표를 여기 한 곳에 둔다.
 * 매핑에 없는 4xx 는 {@link #DEFAULT_MESSAGE} 로 떨어지므로 영어가 나가는 경로 자체가 사라진다.
 *
 * <p>문구는 "무엇이 잘못됐는지"까지만 말한다. 어느 파라미터가 왜 틀렸는지 같은 내부 정보는
 * 넣지 않는다. 그 수준의 안내가 필요한 경우는 이미 {@link BusinessException} 이 자기 메시지를 갖고 있다.
 */
final class ClientErrorMessages {

	/**
	 * 표에 없는 4xx 가 왔을 때 쓰는 문구. 영어가 새어 나가지 않게 하는 것이 이 상수의 목적이다.
	 */
	static final String DEFAULT_MESSAGE = "요청을 처리할 수 없습니다";

	/**
	 * 실제로 이 애플리케이션에서 나올 수 있는 4xx 만 적는다. 쓰이지 않는 상태 코드까지 미리 채워 두면
	 * 검증되지 않은 문구가 쌓이기만 하고, 어차피 기본 문구가 받아 준다.
	 */
	private static final Map<HttpStatus, String> MESSAGES = Map.of(
		HttpStatus.BAD_REQUEST, CommonErrorCode.VALIDATION_ERROR.getDefaultMessage(),
		HttpStatus.UNAUTHORIZED, "로그인이 필요합니다",
		HttpStatus.FORBIDDEN, "접근 권한이 없습니다",
		HttpStatus.NOT_FOUND, "요청하신 페이지를 찾을 수 없습니다",
		HttpStatus.METHOD_NOT_ALLOWED, "잘못된 경로로 요청했습니다",
		HttpStatus.UNSUPPORTED_MEDIA_TYPE, "지원하지 않는 형식입니다",
		// 업로드 용량 초과. 5MB 라는 구체적인 기준까지 알려 줘야 사용자가 다시 시도할 수 있어
		// 기본 문구 대신 ImageErrorCode 의 문구를 그대로 쓴다.
		HttpStatus.CONTENT_TOO_LARGE, ImageErrorCode.IMAGE_TOO_LARGE.getDefaultMessage()
	);

	private ClientErrorMessages() {
	}

	/**
	 * 4xx 가 아닌 값을 넘기면 안 된다. 5xx 는 내부 사정을 감추기 위해 호출부에서 이미
	 * {@code CommonErrorCode.INTERNAL_ERROR} 로 덮고 있고, 2xx·3xx 는 이 경로로 오지 않는다.
	 */
	static String of(HttpStatus status) {
		return MESSAGES.getOrDefault(status, DEFAULT_MESSAGE);
	}
}
