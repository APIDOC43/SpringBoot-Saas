package com.hocs.server.openai.llm.exception;

public class ApiEntriesNullException extends RuntimeException {

	public ApiEntriesNullException(String apiEntryIsEmpty) {
		super(apiEntryIsEmpty);
	}
}
