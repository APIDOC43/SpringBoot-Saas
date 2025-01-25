package com.hocs.server.code_resolver.legacy.extractor.domain;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum SrcSuffix {

	JAVA(".java"),
	ALL("");

	private final String value;

	public String value() {
		return value;
	}
}
