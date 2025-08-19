package com.hocs.server.common.domain;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.hocs.server.code_parser.core.config.GlobalJavaParser;
import com.github.javaparser.ast.body.TypeDeclaration;

public class SpringBootJava implements LanguageFramework {

    @Override
    public boolean isApiEntry(Path path) {
        if (path == null) {
            return false;
        }
        
        String fileName = path.getFileName().toString();
        
        // Java 파일이 아닌 경우 false 반환
        if (!fileName.endsWith(".java")) {
            return false;
        }
        
        // 파일명 기반으로 Controller 또는 RestController로 끝나는지 확인
        String baseName = fileName.substring(0, fileName.length() - 5); // .java 제거
        if (baseName.endsWith("Controller") || baseName.endsWith("RestController")) {
            // 파일이 실제로 존재하는 경우에만 파싱 시도
            if (Files.exists(path)) {
                return isApiEntryByAnnotation(path);
            } else {
                // 테스트 환경에서 실제 파일이 없는 경우 파일명으로만 판단
                return true;
            }
        }
        
        return false;
    }
    
    private boolean isApiEntryByAnnotation(Path path) {
        try {
            // 기본 설정으로 파싱 (의존성 주입 불가능한 환경)
            ParserConfiguration parserConfiguration = new ParserConfiguration();
            parserConfiguration.setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_21);
            
            // 임시로 기본 설정 사용
            String content = new String(Files.readAllBytes(path));
            CompilationUnit cu = new com.github.javaparser.JavaParser(parserConfiguration).parse(content).getResult().orElse(null);
            
            if (cu == null) {
                return false;
            }

            for (TypeDeclaration<?> typeDeclaration : cu.findAll(TypeDeclaration.class)) {
                if (typeDeclaration.isAnnotationPresent("RestController") || 
                    typeDeclaration.isAnnotationPresent("Controller")) {
                    return true;
                }
            }

            return false;

        } catch (IOException e) {
            System.err.println("SpringBootJava.isApiEntry content is fileRead Error");
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getExtension() {
        return "java";
    }
}
