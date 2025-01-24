package com.hocs.server.code_resolver.domain;

import java.nio.file.Path;
import lombok.Getter;

@Getter
public class SourceCode {
	private final Path path;

	public SourceCode(Path path) {
		this.path = path;
	}
}
