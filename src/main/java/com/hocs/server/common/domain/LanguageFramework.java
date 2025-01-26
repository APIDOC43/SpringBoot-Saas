package com.hocs.server.common.domain;

import java.nio.file.Path;

public interface LanguageFramework {
	boolean isApiEntry(Path path);

	String getExtension();

}
