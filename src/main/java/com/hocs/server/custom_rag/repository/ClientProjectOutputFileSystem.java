package com.hocs.server.custom_rag.repository;

import com.hocs.server.common.domain.LanguageFramework;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Repository;

@Repository
public class ClientProjectOutputFileSystem implements ClientProjectOutput{

	@Override
	public List<Path> findPathList(File rootDir, LanguageFramework languageFramework) {
		List<Path> files = new ArrayList<>();
		for (File file : Objects.requireNonNull(rootDir.listFiles())) {

			if (file.isDirectory()) {
				files.addAll(findPathList(file, languageFramework));
			} else {
				if (file.getName().endsWith(languageFramework.getExtension())) {
					files.add(file.toPath());
				}
			}
		}

		return files;
	}
}