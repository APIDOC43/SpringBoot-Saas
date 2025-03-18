package com.hocs.server.api_spec_generator.llm.exception;

public class ApiEntriesNullException extends RuntimeException {

	public ApiEntriesNullException(String apiEntryIsEmpty) {
		super(apiEntryIsEmpty);
	}
}
