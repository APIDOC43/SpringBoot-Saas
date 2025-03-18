package com.hocs.server.api_spec_generator.llm.exception;

public class LLMException extends RuntimeException {

	public LLMException(String tpm) {
		super(tpm);
	}
}
