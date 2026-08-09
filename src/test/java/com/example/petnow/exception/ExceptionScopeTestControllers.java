package com.example.petnow.exception;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 예외 처리 담당 범위 검증용 프로브 컨트롤러 모음.
 *
 * <p>실제 컨트롤러를 쓰면 서비스/매퍼까지 끌려오므로, 예외만 던지는 최소 컨트롤러를 둔다.
 * MvcProbeController 는 일부러 {@code MvcExceptionHandler} 의 예전 assignableTypes
 * 화이트리스트에 없던 "새로 추가된 뷰 컨트롤러" 역할을 한다.
 */
final class ExceptionScopeTestControllers {

	static final String BUSINESS_MESSAGE = "테스트 비즈니스 예외";

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
	}

	@RestController
	static class RestProbeController {

		@GetMapping("/probe/rest/boom")
		String boom() {
			throw new IllegalStateException("REST 핸들러에서 터진 예외");
		}
	}
}
