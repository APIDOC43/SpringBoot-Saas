package com.hocs.server.common.domain;

public class LanguageFrameworkFactory {
    public static LanguageFramework create(CodingLanguage language, ProjectFramework framework) {
        if (language == null) {
            throw new IllegalArgumentException("Language cannot be null");
        }
        if (framework == null) {
            throw new IllegalArgumentException("Framework cannot be null");
        }
        
        if (language == CodingLanguage.JAVA && 
            (framework == ProjectFramework.SPRINGBOOT || framework == ProjectFramework.SPRING_BOOT)) {
            return new SpringBootJava();
        } else if (language == CodingLanguage.JAVASCRIPT && framework == ProjectFramework.NODE_JS) {
            return new NodeJs();
        }
        
        throw new IllegalArgumentException("Unsupported combination: " + language + ", " + framework);
    }
}
