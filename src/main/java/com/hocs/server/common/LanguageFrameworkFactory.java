package com.hocs.server.common;

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