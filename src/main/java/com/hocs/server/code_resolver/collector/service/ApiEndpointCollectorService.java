package com.hocs.server.code_resolver.collector.service;

import com.hocs.server.code_resolver.collector.domain.LanguageFramework;
import com.hocs.server.code_resolver.collector.domain.LanguageFrameworkFactory;
import com.hocs.server.saas_v2.domain.ClientProjectPath;
import com.hocs.server.saas_v2.domain.CodingLanguage;
import com.hocs.server.saas_v2.domain.ProjectFramework;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ApiEndpointCollectorService {

	public List<File> findControllerFiles(CodingLanguage language, ProjectFramework
		framework, ClientProjectPath path) {
		LanguageFramework languageFramework = LanguageFrameworkFactory.create(language, framework);

		List<File> files = collectFiles(path.getPath().toFile(), languageFramework.getExtension());

		return files.stream()
			.filter(f -> languageFramework.isApiEntry(f.toPath()))
			.collect(Collectors.toList());
	}


	private List<File> collectFiles(File dir, String srcSuffix) {
		List<File> javaFiles = new ArrayList<>();

		for (File file : Objects.requireNonNull(dir.listFiles())) {
			if (file.isDirectory()) {
				javaFiles.addAll(collectFiles(file, srcSuffix));
			} else {
				if (file.getName().endsWith("." + srcSuffix)) {
					javaFiles.add(file);
				}
			}
		}

		return javaFiles;
	}
}
