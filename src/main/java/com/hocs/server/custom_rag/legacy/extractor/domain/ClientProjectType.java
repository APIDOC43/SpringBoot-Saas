package com.hocs.server.custom_rag.legacy.extractor.domain;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum ClientProjectType {
	SPRING_JAVA("src/main/java",SrcSuffix.JAVA);

	final String srcRootPath;
	final SrcSuffix srcSuffix;

	public String srcRootPath() {
		return srcRootPath;
	}

	public SrcSuffix srcSuffix() {
		return srcSuffix;
	}
}
