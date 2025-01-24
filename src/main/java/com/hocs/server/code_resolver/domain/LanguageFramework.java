package com.hocs.server.code_resolver.domain;

import java.nio.file.Path;

public interface LanguageFramework {
	boolean isApiEntry(Path path);

	String getExtension();

}
