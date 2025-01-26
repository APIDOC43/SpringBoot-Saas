package com.hocs.server.code_resolver.service;

import com.hocs.server.code_resolver.domain.APIEntries;
import com.hocs.server.common.domain.LanguageFramework;
import com.hocs.server.common.domain.LanguageFrameworkFactory;
import com.hocs.server.common.domain.ClientProjectPath;
import com.hocs.server.common.domain.CodingLanguage;
import com.hocs.server.common.domain.ProjectFramework;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ApiEndpointCollectorService {

	public APIEntries findControllerFiles(CodingLanguage language, ProjectFramework
		framework, ClientProjectPath rootPath) {

		LanguageFramework languageFramework = LanguageFrameworkFactory.create(language, framework);
		return APIEntries.create(rootPath, languageFramework);
	}
}
