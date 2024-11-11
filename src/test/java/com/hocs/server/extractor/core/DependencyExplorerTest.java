package com.hocs.server.extractor.core;

import static org.junit.jupiter.api.Assertions.*;


import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.hocs.server.extractor.core.data.JavaClassifiedDataContainer;
import com.hocs.server.extractor.core.util.GenericTypeResolver;
import com.hocs.server.extractor.core.util.GroupingStrategy;
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
	private JavaClassifiedDataContainer javaClassifiedDataContainer;

	@Mock
	private ExpressionResolver expressionResolver;

	@Mock
	private GenericTypeResolver genericTypeResolver;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		dependencyExplorer = new DependencyExplorer(javaClassifiedDataContainer, expressionResolver,genericTypeResolver);
	}

	@Test
	void testFindClassDependencies_SimpleCase() throws Exception {
		// 준비: 클래스 A가 클래스 B에 의존한다고 가정
		String classAContent = "public class A { private B b; }";
		String classBContent = "public class B { }";

		// 임시 파일 생성
		Path tempDir = Files.createTempDirectory("testClasses");
		Path classAPath = tempDir.resolve("A.java");
		Path classBPath = tempDir.resolve("B.java");
		Files.writeString(classAPath, classAContent);
		Files.writeString(classBPath, classBContent);

		// JavaClassifiedDataContainer 설정
		Map<String, String> classToFilePath = new HashMap<>();
		classToFilePath.put("A", classAPath.toString());
		classToFilePath.put("B", classBPath.toString());
		when(javaClassifiedDataContainer.getClassToFilePath()).thenReturn(classToFilePath);
		when(genericTypeResolver.extractClassNamesFromType(any())).thenAnswer(invocation -> List.of((String)invocation.getArgument(0)));

		// 테스트 실행
		Set<String> requiredFiles = new HashSet<>();
		Set<String> visitedClasses = new HashSet<>();
		dependencyExplorer.findClassDependencies("A", requiredFiles, visitedClasses);

		assertTrue(requiredFiles.contains(classAPath.toString()));
		assertTrue(requiredFiles.contains(classBPath.toString()));
		assertEquals(2, requiredFiles.size());

		// 임시 파일 삭제
		Files.deleteIfExists(classAPath);
		Files.deleteIfExists(classBPath);
		Files.deleteIfExists(tempDir);
	}


	@Test
	void testFindVariableDeclarations() throws Exception {
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

	@Test
	void testFindMethodCallDependencies() throws Exception {
		// 준비: 클래스 E의 메서드가 클래스 F의 메서드를 호출한다고 가정
		String classEContent = "public class E { public void methodE() { F f = new F(); f.methodF(); } }";
		String classFContent = "public class F { public void methodF() { } }";

		// 임시 파일 생성
		Path tempDir = Files.createTempDirectory("testClasses");
		Path classEPath = tempDir.resolve("E.java");
		Path classFPath = tempDir.resolve("F.java");
		Files.writeString(classEPath, classEContent);
		Files.writeString(classFPath, classFContent);

		// JavaClassifiedDataContainer 설정
		Map<String, String> classToFilePath = new HashMap<>();
		classToFilePath.put("E", classEPath.toString());
		classToFilePath.put("F", classFPath.toString());
		when(javaClassifiedDataContainer.getClassToFilePath()).thenReturn(classToFilePath);

		// ExpressionResolver 모의 객체 설정
		when(expressionResolver.resolveExpressionType(any(), eq("E")))
			.thenReturn(Optional.of("F"));

		// 테스트 실행
		Set<String> requiredFiles = new HashSet<>();
		Set<String> visitedMethods = new HashSet<>();
		dependencyExplorer.findMethodCallDependencies("E", "methodE", requiredFiles, visitedMethods);

		// 검증
		assertTrue(requiredFiles.contains(classFPath.toString()));
		assertEquals(1, requiredFiles.size());

		// 임시 파일 삭제
		Files.deleteIfExists(classEPath);
		Files.deleteIfExists(classFPath);
		Files.deleteIfExists(tempDir);
	}
}
