package com.hocs.server.code_parser.service;

import com.hocs.server.code_parser.domain.APIEntries;
import com.hocs.server.code_parser.domain.SourceFile;
import com.hocs.server.code_parser.repository.ClientProjectOutput;
import com.hocs.server.common.domain.ClientProjectPath;
import com.hocs.server.common.domain.CodingLanguage;
import com.hocs.server.common.domain.LanguageFramework;
import com.hocs.server.common.domain.LanguageFrameworkFactory;
import com.hocs.server.common.domain.ProjectFramework;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ApiEndpointCollectorService {

	private final ClientProjectOutput clientProjectOutput;
	public APIEntries findControllerFiles(CodingLanguage language, ProjectFramework
		framework, ClientProjectPath rootPath) {

		LanguageFramework languageFramework = LanguageFrameworkFactory.create(language, framework);

		List<SourceFile> sourceFiles = clientProjectOutput.findPathList(rootPath.getPath().toFile(),
				languageFramework)
			.stream()
			.filter(languageFramework::isApiEntry)
			.map(SourceFile::new)
			.collect(Collectors.toList());

		return new APIEntries(sourceFiles);
	}
}
