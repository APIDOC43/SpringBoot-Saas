package com.hocs.server.code_parser.core.config;

// 이 클래스는 GlobalJavaParser로 대체되었습니다.
// ExtractorConfig는 더 이상 사용되지 않습니다.

/*
import java.io.File;

import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ParserConfiguration.LanguageLevel;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ExtractorConfig {
	@PostConstruct
	public void setConfig(String SOURCE_ROOT) {
		// 언어 레벨 설정 및 심볼 솔버 구성
		ParserConfiguration parserConfiguration = new ParserConfiguration();
		parserConfiguration.setLanguageLevel(LanguageLevel.JAVA_21);

		// 타입 솔버 설정 (소스 코드와 클래스패스를 모두 포함)
		CombinedTypeSolver typeSolver = new CombinedTypeSolver();
		typeSolver.add(new JavaParserTypeSolver(new File(SOURCE_ROOT)));
		typeSolver.add(new ReflectionTypeSolver());

		parserConfiguration.setSymbolResolver(new JavaSymbolSolver(typeSolver));
		StaticJavaParser.setConfiguration(parserConfiguration);
	}
}
*/
