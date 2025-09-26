package com.hocs.server.extractor.core;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.hocs.server.code_parser.core.config.GlobalJavaParser;
import com.hocs.server.code_parser.core.service.DependencyAnalyzer;
import com.hocs.server.code_parser.core.service.DependencyExplorer;
import com.hocs.server.code_parser.core.dataobject.JavaClassifiedDataContainer;
import com.hocs.server.code_parser.core.domain.API;
import com.hocs.server.code_parser.core.domain.ApiEndpoint;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DependencyAnalyzerTest {

	private DependencyExplorer dependencyExplorer;
	private DependencyAnalyzer dependencyAnalyzer;
	private GlobalJavaParser globalJavaParser;

	private Path tempDir;

	@BeforeEach
	public void setUp() throws IOException {
		// Mock 객체 생성
		dependencyExplorer = mock(DependencyExplorer.class);
		dependencyAnalyzer = new DependencyAnalyzer(dependencyExplorer, globalJavaParser);

		// 임시 디렉토리 생성
		tempDir = Files.createTempDirectory("test");
	}

	@AfterEach
	public void tearDown() throws IOException {
		// 임시 디렉토리 삭제
		Files.walk(tempDir)
			.sorted(Comparator.reverseOrder())
			.forEach(
				path -> {
					try {
						Files.delete(path);
					} catch (IOException e) {
						e.printStackTrace();
					}
				});
	}

	@Test
	public void testFindDependency_nullFilePath() throws Exception {
		// 준비
		String className = "com.example.UnknownController";

		// 실행
		List<API> apis = dependencyAnalyzer.findDependency(className, new JavaClassifiedDataContainer());

		// 검증
		assertNull(apis);
	}
}
