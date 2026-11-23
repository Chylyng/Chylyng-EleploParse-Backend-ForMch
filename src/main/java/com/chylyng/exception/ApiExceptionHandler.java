package com.chylyng.exception;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class ApiExceptionHandler {
	/*
	 * 處理Exception異常
	 */
	@ExceptionHandler(Exception.class)
	public void handleException(Exception e) {
		log.warn("cause = " + e);
	}

	/*
	 * 處理RuntimeException異常
	 */
	@ExceptionHandler(RuntimeException.class)
	public void handleRuntimeException(RuntimeException e) {
		log.warn("cause = " + e);
	}

}
