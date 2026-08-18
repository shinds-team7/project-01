package com.example.petnow.exception;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

/**
 * 예외 처리 담당 범위 검증용 프로브 컨트롤러 모음.
 *
 * <p>실제 컨트롤러를 쓰면 서비스/매퍼까지 끌려오므로, 예외만 던지는 최소 컨트롤러를 둔다.
 * MvcProbeController 는 일부러 {@code MvcExceptionHandler} 의 예전 assignableTypes
 * 화이트리스트에 없던 "새로 추가된 뷰 컨트롤러" 역할을 한다.
 */
final class ExceptionScopeTestControllers {

	static final String BUSINESS_MESSAGE = "테스트 비즈니스 예외";

	static final long MAX_UPLOAD_SIZE = 5L * 1024 * 1024;

	private ExceptionScopeTestControllers() {
	}

	@Controller
	static class MvcProbeController {

		@GetMapping("/probe/mvc/boom")
		String boom() {
			throw new IllegalStateException("렌더링 전 핸들러에서 터진 예외");
		}

		@GetMapping("/probe/mvc/business")
		String business() {
			throw new BusinessException(CommonErrorCode.VALIDATION_ERROR, BUSINESS_MESSAGE);
		}

		/**
		 * 용량 초과는 서블릿 컨테이너가 멀티파트를 파싱하다 던지는 예외라 MockMvc 로는 재현되지 않는다.
		 * 핸들러까지 도달했을 때 어떤 화면이 나가는지만 확인하려고 직접 던진다.
		 */
		@GetMapping("/probe/mvc/upload-too-large")
		String uploadTooLarge() {
			throw new MaxUploadSizeExceededException(MAX_UPLOAD_SIZE);
		}
	}

	@RestController
	static class RestProbeController {

		@GetMapping("/probe/rest/boom")
		String boom() {
			throw new IllegalStateException("REST 핸들러에서 터진 예외");
		}
	}
}
