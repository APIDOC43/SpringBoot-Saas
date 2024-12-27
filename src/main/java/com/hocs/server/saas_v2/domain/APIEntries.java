package com.hocs.server.saas_v2.domain;

import com.hocs.server.saas_v2.domain.language_framwork.LanguageFramework;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class APIEntries {

	private List<SourceCode> sourceCodeList;
	private final Path rootPath;
	private final LanguageFramework languageFramework;

	public APIEntries(Path rootPath, LanguageFramework languageFramework) {
		this.rootPath = rootPath;
		this.languageFramework = languageFramework;
	}

	public void initProcess() {
		List<Path> srcPath = read(rootPath.toFile());
		sourceCodeList = new ArrayList<>();

		for (Path path : srcPath) {
			if(languageFramework.isApiEntry(path)){
				sourceCodeList.add(new SourceCode(path));
			}
		}

	}

	private List<Path> read(File rootDir) {
		List<Path> files = new ArrayList<>();
		for (File file : Objects.requireNonNull(rootDir.listFiles())) {

			if (file.isDirectory()) {
				files.addAll(read(file));
			} else {
				if (file.getName().endsWith(languageFramework.getExtension())) {
					files.add(file.toPath());
				}
			}
		}

		return files;
	}
}
