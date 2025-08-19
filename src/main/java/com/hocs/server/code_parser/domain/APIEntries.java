package com.hocs.server.code_parser.domain;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

public class APIEntries {

	private final List<SourceFile> sourceFileList;

	public APIEntries(List<SourceFile> sourceFileList) {
		this.sourceFileList = sourceFileList;
	}

	public List<File> getFiles() {
		return this.sourceFileList
			.stream()
			.map(m -> m.getPath().toFile())
			.collect(Collectors.toList());
	}

	// 테스트 호환성을 위한 메소드
	public List<SourceFile> getEntries() {
		return this.sourceFileList;
	}
}
