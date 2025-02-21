package com.hocs.server.front_server.common.exception;

import java.time.LocalDateTime;
import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class RestGlobalExceptionHandler {

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<Object> handleInvalidEnum(HttpMessageNotReadableException ex) {
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
			new ErrorResponse(
				"Invalid request",
				"Invalid value for enum field",
				LocalDateTime.now()
			)
		);
	}


}

@Data
class ErrorResponse {
	private String error;
	private String message;
	private LocalDateTime timestamp;

	public ErrorResponse(String error, String message, LocalDateTime timestamp) {
		this.error = error;
		this.message = message;
		this.timestamp = timestamp;
	}
}
