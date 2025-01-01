package com.hocs.server.code_resolver.collector.domain;

import com.hocs.server.code_resolver.service.out.language_framwork.adapter.SpringBootJava;
import com.hocs.server.saas_v2.domain.CodingLanguage;
import com.hocs.server.saas_v2.domain.ProjectFramework;

public class LanguageFrameworkFactory {
	public static LanguageFramework create(CodingLanguage language, ProjectFramework framework) {
		if (language == CodingLanguage.JAVA && framework == ProjectFramework.SPRINGBOOT) {
			return new SpringBootJava();
//		} else if (language == CodingLanguage.PYTHON && framework == ProjectFramework.DJANGO) {
//			return new DjangoPython();
		}
		throw new IllegalArgumentException("Unsupported combination: " + language + ", " + framework);
	}
}