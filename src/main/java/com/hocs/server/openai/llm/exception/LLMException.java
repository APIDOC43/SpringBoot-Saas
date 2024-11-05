package com.hocs.server.openai.llm.exception;

public class LLMException extends RuntimeException {

	public LLMException(String tpm) {
		super(tpm);
	}
}
