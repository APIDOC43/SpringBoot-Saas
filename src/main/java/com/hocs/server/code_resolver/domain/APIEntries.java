package com.hocs.server.code_resolver.domain;

import com.hocs.server.saas_v2.domain.ClientProjectPath;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class APIEntries {

	private List<SourceCode> sourceCodeList;

	private APIEntries(List<SourceCode> sourceCodeList) {
		this.sourceCodeList = sourceCodeList;
	}

	public static APIEntries create(ClientProjectPath rootPath, LanguageFramework languageFramework) {
		List<Path> srcPath = read(rootPath.getPath().toFile(),languageFramework);
		List<SourceCode> sourceCodeList = new ArrayList<>();

		for (Path path : srcPath) {
			if(languageFramework.isApiEntry(path)){
				sourceCodeList.add(new SourceCode(path));
			}
		}

		return new APIEntries(sourceCodeList);
	}

	private static List<Path> read(File rootDir, LanguageFramework languageFramework) {
		List<Path> files = new ArrayList<>();
		for (File file : Objects.requireNonNull(rootDir.listFiles())) {

			if (file.isDirectory()) {
				files.addAll(read(file,languageFramework));
			} else {
				if (file.getName().endsWith(languageFramework.getExtension())) {
					files.add(file.toPath());
				}
			}
		}

		return files;
	}

	public List<File> getControllerFiles(){
		return this.sourceCodeList
			.stream()
			.map(m->m.getPath().toFile())
			.collect(Collectors.toList());
	}
}
