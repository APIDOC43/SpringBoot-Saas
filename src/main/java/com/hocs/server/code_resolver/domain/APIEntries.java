package com.hocs.server.code_resolver.domain;

import com.hocs.server.common.LanguageFramework;
import com.hocs.server.saas_v2.domain.ClientProjectPath;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class APIEntries {

	private final List<SourceFile> sourceFileList;

	private APIEntries(List<SourceFile> sourceFileList) {
		this.sourceFileList = sourceFileList;
	}

	public static APIEntries create(ClientProjectPath rootPath, LanguageFramework languageFramework) {
		List<Path> srcPath = read(rootPath.getPath().toFile(),languageFramework);
		List<SourceFile> sourceFileList = new ArrayList<>();

		for (Path path : srcPath) {
			if(languageFramework.isApiEntry(path)){
				sourceFileList.add(new SourceFile(path));
			}
		}

		return new APIEntries(sourceFileList);
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

	public List<File> getFiles(){
		return this.sourceFileList
			.stream()
			.map(m->m.getPath().toFile())
			.collect(Collectors.toList());
	}
}
