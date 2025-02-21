package com.hocs.server.custom_rag.repository;

import com.hocs.server.common.domain.LanguageFramework;
import java.io.File;
import java.nio.file.Path;
import java.util.List;

public interface ClientProjectOutput {

	List<Path> findPathList(File rootDir, LanguageFramework languageFramework);
}