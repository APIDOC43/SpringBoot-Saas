package com.hocs.server.saas_v2.exception;

public class CustomException extends RuntimeException {

	public CustomException(ErrorCode code) {
		super(code.message);
	}
}
