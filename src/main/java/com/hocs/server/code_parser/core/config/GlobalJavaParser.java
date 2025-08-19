package com.hocs.server.code_parser.core.config;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ParserConfiguration.LanguageLevel;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

@Slf4j
@Component
public class GlobalJavaParser {
    
    private final Object configLock = new Object();
    private volatile boolean configured = false;
    
    /**
     * JavaParser 설정을 sourceRoot로 초기화
     */
    public void configure(String sourceRoot) {
        synchronized (configLock) {
            log.info("Configuring JavaParser with source root: {}", sourceRoot);
            
            ParserConfiguration parserConfiguration = new ParserConfiguration();
            parserConfiguration.setLanguageLevel(LanguageLevel.JAVA_21);

            CombinedTypeSolver typeSolver = new CombinedTypeSolver();
            
            if (sourceRoot != null && !sourceRoot.trim().isEmpty()) {
                File sourceFile = new File(sourceRoot);
                if (sourceFile.exists() && sourceFile.isDirectory()) {
                    typeSolver.add(new JavaParserTypeSolver(sourceFile));
                    log.info("Added source root to type solver: {}", sourceRoot);
                } else {
                    log.warn("Source root does not exist or is not a directory: {}", sourceRoot);
                }
            }
            
            typeSolver.add(new ReflectionTypeSolver());
            parserConfiguration.setSymbolResolver(new JavaSymbolSolver(typeSolver));
            
            StaticJavaParser.setConfiguration(parserConfiguration);
            configured = true;
            log.info("JavaParser configured successfully for all threads");
        }
    }
    
    /**
     * 기본 설정으로 초기화 (sourceRoot 없이)
     */
    public void configureDefault() {
        synchronized (configLock) {
            log.info("Configuring JavaParser with default settings");
            
            ParserConfiguration parserConfiguration = new ParserConfiguration();
            parserConfiguration.setLanguageLevel(LanguageLevel.JAVA_21);

            CombinedTypeSolver typeSolver = new CombinedTypeSolver();
            typeSolver.add(new ReflectionTypeSolver());
            parserConfiguration.setSymbolResolver(new JavaSymbolSolver(typeSolver));
            
            StaticJavaParser.setConfiguration(parserConfiguration);
            configured = true;
            log.info("JavaParser configured with default settings");
        }
    }
    
    /**
     * 문자열 코드 파싱
     */
    public CompilationUnit parse(String code) {
        ensureConfigured();
        return StaticJavaParser.parse(code);
    }
    
    /**
     * 파일 파싱
     */
    public CompilationUnit parse(File file) throws FileNotFoundException {
        ensureConfigured();
        return StaticJavaParser.parse(file);
    }
    
    /**
     * InputStream 파싱
     */
    public CompilationUnit parse(InputStream inputStream) {
        ensureConfigured();
        return StaticJavaParser.parse(inputStream);
    }

    
    private void ensureConfigured() {
        if (!configured) {
            configureDefault();
        }
    }
}
