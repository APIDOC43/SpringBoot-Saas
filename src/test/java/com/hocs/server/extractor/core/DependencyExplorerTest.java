package com.hocs.server.extractor.core;

import static org.junit.jupiter.api.Assertions.*;


import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.hocs.server.code_parser.core.config.GlobalJavaParser;
import com.hocs.server.code_parser.core.service.DependencyExplorer;
import com.hocs.server.code_parser.core.service.ExpressionResolver;
import com.hocs.server.code_parser.core.dataobject.JavaClassifiedDataContainer;
import com.hocs.server.code_parser.core.util.GenericTypeResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static org.mockito.Mockito.*;

class DependencyExplorerTest {

	private DependencyExplorer dependencyExplorer;

	@Mock
	private ExpressionResolver expressionResolver;

	@Mock
	private GenericTypeResolver genericTypeResolver;

	@Mock
	private GlobalJavaParser globalJavaParser;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		dependencyExplorer = new DependencyExplorer(expressionResolver, genericTypeResolver, globalJavaParser);
	}

	@Test
	void testFindVariableDeclarations()  {
		// 준비: 메서드 내에 변수 선언이 있는 경우
		String methodContent = "public void testMethod() { int x = 0; String s = \"hello\"; }";
		CompilationUnit cu = com.github.javaparser.StaticJavaParser.parse("public class Temp { " + methodContent + " }");
		MethodDeclaration method = cu.findFirst(MethodDeclaration.class).get();

		// 메서드 실행
		List<String> varTypes = dependencyExplorer.findVariableDeclarations(method);

		// 검증
		assertEquals(2, varTypes.size());
		assertTrue(varTypes.contains("int"));
		assertTrue(varTypes.contains("String"));
	}
}
