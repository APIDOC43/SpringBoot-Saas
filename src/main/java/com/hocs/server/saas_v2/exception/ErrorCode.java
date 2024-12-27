package com.hocs.server.saas_v2.exception;

public enum ErrorCode {

	GIT_REPOSITORY_IS_EMPTY("G00001","조회가능한 GitHub Repository가 없습니다.");

	final String code;
	final String message;

	ErrorCode(String code, String message) {
		this.code = code;
		this.message = message;
	}
}
