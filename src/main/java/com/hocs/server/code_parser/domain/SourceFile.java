package com.hocs.server.code_parser.domain;

import java.nio.file.Path;
import lombok.Getter;

@Getter
public class SourceFile {
	protected final Path path;

	public SourceFile(Path path) {
		this.path = path;
	}
}
