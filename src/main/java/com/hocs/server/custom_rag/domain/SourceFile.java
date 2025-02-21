package com.hocs.server.custom_rag.domain;

import java.nio.file.Path;
import lombok.Getter;

@Getter
public class SourceFile {
	protected final Path path;

	public SourceFile(Path path) {
		this.path = path;
	}
}
