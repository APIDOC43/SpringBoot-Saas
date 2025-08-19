package com.hocs.server.saas_platform.common.exception;

public enum ErrorCode {

	//git
	GIT_REPOSITORY_IS_EMPTY("G00001","조회가능한 GitHub Repository가 없습니다."),
	GIT_CLONE_FAIL("G00002","Git Clone에 실패하였습니다."),


	//IO
	IO_CREATE_DIR_FAIL("I00001", "폴더 생성에 실패하였습니다."),

	//404
	NOT_FOUND_EXCEPTION("N00001", "존재하지 않는 ID입니다."),

	//400
	INVALID_REQUEST_EXCEPTION("B00001", "잘못된 요청입니다.");

	final String code;
	final String message;

	ErrorCode(String code, String message) {
		this.code = code;
		this.message = message;
	}
}
