package com.hocs.server.saas_platform.common.exception;

public class CustomException extends RuntimeException {

	public CustomException(ErrorCode code) {
		super(code.message);
	}
}
