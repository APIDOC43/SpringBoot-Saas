package com.hocs.server.front_server.common.exception;

public class CustomException extends RuntimeException {

	public CustomException(ErrorCode code) {
		super(code.message);
	}
}
