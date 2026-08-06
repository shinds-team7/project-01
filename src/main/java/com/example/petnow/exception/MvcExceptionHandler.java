package com.example.petnow.exception;

import org.springframework.ui.Model;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.example.petnow.common.controller.HomeController;
import com.example.petnow.controller.PetController;
import com.example.petnow.controller.HostController;
import com.example.petnow.controller.PlaceController;
import com.example.petnow.controller.AuthController;

import lombok.extern.slf4j.Slf4j;

/**
 * 뷰(HTML)를 렌더링하는 @Controller 전용 예외 처리기.
 * annotations = Controller.class로 스코프하면 @RestController(내부적으로 @Controller를
 * 메타 애노테이션으로 가짐)까지 걸려 GlobalExceptionHandler와 충돌하므로,
 * 대상 MVC 컨트롤러를 assignableTypes로 명시한다. 새 뷰 컨트롤러를 추가하면 여기에도 등록할 것.
 */
@Slf4j
@ControllerAdvice(assignableTypes = {
	HomeController.class,
	PetController.class,
	HostController.class,
	PlaceController.class,
	AuthController.class
})
public class MvcExceptionHandler {

	@ExceptionHandler(BusinessException.class)
	public String handleBusiness(BusinessException e, Model model) {
		ErrorCode ec = e.getErrorCode();
		log.warn("BusinessException: {} {}", ec.getCode(), e.getMessage());
		model.addAttribute("status", ec.getStatus().value());
		model.addAttribute("message", e.getMessage());
		return "error";
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public String handleValidation(MethodArgumentNotValidException e, Model model) {
		String message = e.getBindingResult().getFieldErrors().stream()
			.findFirst()
			.map(fe -> fe.getField() + " : " + fe.getDefaultMessage())
			.orElse(CommonErrorCode.VALIDATION_ERROR.getDefaultMessage());

		model.addAttribute("status", CommonErrorCode.VALIDATION_ERROR.getStatus().value());
		model.addAttribute("message", message);
		return "error";
	}

	@ExceptionHandler(Exception.class)
	public String handleException(Exception e, Model model) {
		log.error("Unhandled exception", e);
		model.addAttribute("status", CommonErrorCode.INTERNAL_ERROR.getStatus().value());
		model.addAttribute("message", CommonErrorCode.INTERNAL_ERROR.getDefaultMessage());
		return "error";
	}
}
