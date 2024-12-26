package com.hocs.server.saas_v2;

public class CustomException extends RuntimeException {

	public CustomException(ErrorCode code) {
		super(code.message);
	}
}
