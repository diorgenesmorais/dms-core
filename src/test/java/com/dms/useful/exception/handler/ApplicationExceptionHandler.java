package com.dms.useful.exception.handler;

import org.springframework.context.MessageSource;
import org.springframework.web.bind.annotation.ControllerAdvice;

@ControllerAdvice
public class ApplicationExceptionHandler extends ResourcesExceptionHandler {

	public ApplicationExceptionHandler(MessageSource messageSource) {
		super(messageSource);
	}

}
