package com.hocs.server.extractor.core;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.hocs.server.extractor.core.data.JavaClassifiedDataContainer;
import com.hocs.server.extractor.domain.API;
import com.hocs.server.extractor.domain.ApiEndpoint;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DependencyAnalyzerTest {

	private JavaClassifiedDataContainer javaClassifiedDataContainer;
	private DependencyExplorer dependencyExplorer;
	private DependencyAnalyzer dependencyAnalyzer;

	private Path tempDir;

	@BeforeEach
	public void setUp() throws IOException {
		// Mock 객체 생성
		javaClassifiedDataContainer = mock(JavaClassifiedDataContainer.class);
		dependencyExplorer = mock(DependencyExplorer.class);
		dependencyAnalyzer = new DependencyAnalyzer(javaClassifiedDataContainer, dependencyExplorer);

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
	public void testFindDependency_singleMethod() throws Exception {
		// 준비
		String className = "MyController";
		String fileName = "MyController.java";
		String filePath = tempDir.resolve(fileName).toString();

		// Mock 데이터 설정
		Map<String, String> classToFilePathMap = new HashMap<>();
		classToFilePathMap.put(className, filePath);
		when(javaClassifiedDataContainer.getClassToFilePath()).thenReturn(classToFilePathMap);

		String srcContent =
			"package com.example;\n"
				+ "import org.springframework.web.bind.annotation.GetMapping;\n"
				+ "import org.springframework.web.bind.annotation.RestController;\n"
				+ "\n"
				+ "@RestController\n"
				+ "public class MyController {\n"
				+ "    @GetMapping(\"/api/test\")\n"
				+ "    public String test() {\n"
				+ "        return \"Hello\";\n"
				+ "    }\n"
				+ "}";

		Files.writeString(tempDir.resolve(fileName), srcContent);

		// dependencyExplorer의 동작 모킹
		doAnswer(
			invocation -> {
				String classNameArg = invocation.getArgument(0);
				Set<String> requiredFiles = invocation.getArgument(1);
				// 클래스 이름에 따라 의존성 파일 추가 시뮬레이션
				if (classNameArg.equals("MyController")) {
					requiredFiles.add(filePath); // 컨트롤러 클래스 파일 자체
				} else if (classNameArg.equals("String")) {
					requiredFiles.add("/path/to/String.java");
				}
				return null;
			})
			.when(dependencyExplorer)
			.findClassDependencies(anyString(), anySet(), anySet());

		// 실행
		List<API> apis = dependencyAnalyzer.findDependency(className);

		// 검증
		assertNotNull(apis);
		assertEquals(1, apis.size());

		API api = apis.get(0);
		ApiEndpoint endpoint = api.getApiEndpoint();

		assertEquals("GET", endpoint.getMethod());
		assertEquals("/api/test", endpoint.getApi());

		List<String> requiredFiles = api.getPaths();
		assertTrue(requiredFiles.contains(filePath));
		assertTrue(requiredFiles.contains("/path/to/String.java"));

		// dependencyExplorer.findClassDependencies가 예상된 클래스 이름으로 호출되었는지 확인
		verify(dependencyExplorer).findClassDependencies(eq("MyController"), anySet(), anySet());
		verify(dependencyExplorer).findClassDependencies(eq("String"), anySet(), anySet());
	}

	@Test
	public void testFindDependency_withSupportedAnnotations() throws Exception {
		// 준비
		String className = "MyControllerWithParams";
		String fileName = "MyControllerWithParams.java";
		String filePath = tempDir.resolve(fileName).toString();

		// Mock 데이터 설정
		Map<String, String> classToFilePathMap = new HashMap<>();
		classToFilePathMap.put(className, filePath);
		when(javaClassifiedDataContainer.getClassToFilePath()).thenReturn(classToFilePathMap);

		String srcContent =
			"package com.example;\n"
				+ "import org.springframework.web.bind.annotation.PostMapping;\n"
				+ "import org.springframework.web.bind.annotation.RequestBody;\n"
				+ "import org.springframework.web.bind.annotation.PathVariable;\n"
				+ "import org.springframework.web.bind.annotation.RequestParam;\n"
				+ "import org.springframework.web.bind.annotation.RestController;\n"
				+ "import org.springframework.web.multipart.MultipartFile;\n"
				+ "\n"
				+ "import com.example.dto.MyRequestDto;\n"
				+ "import com.example.dto.MyResponseDto;\n"
				+ "\n"
				+ "@RestController\n"
				+ "public class MyControllerWithParams {\n"
				+ "    @PostMapping(\"/api/test/{id}\")\n"
				+ "    public MyResponseDto test(@RequestBody MyRequestDto request,\n"
				+ "                              @PathVariable(\"id\") String id,\n"
				+ "                              @RequestParam(\"file\") MultipartFile file) {\n"
				+ "        return new MyResponseDto();\n"
				+ "    }\n"
				+ "}";

		Files.writeString(tempDir.resolve(fileName), srcContent);

		// dependencyExplorer의 동작 모킹
		doAnswer(
			invocation -> {
				String classNameArg = invocation.getArgument(0);
				Set<String> requiredFiles = invocation.getArgument(1);
				// 클래스 이름에 따라 의존성 파일 추가 시뮬레이션
				if (classNameArg.equals("MyControllerWithParams")) {
					requiredFiles.add(filePath); // 컨트롤러 클래스 파일 자체
				} else if (classNameArg.equals("MyRequestDto")) {
					requiredFiles.add("/path/to/MyRequestDto.java");
				} else if (classNameArg.equals("MyResponseDto")) {
					requiredFiles.add("/path/to/MyResponseDto.java");
				}
				return null;
			})
			.when(dependencyExplorer)
			.findClassDependencies(anyString(), anySet(), anySet());

		// 실행
		List<API> apis = dependencyAnalyzer.findDependency(className);

		// 검증
		assertNotNull(apis);
		assertEquals(1, apis.size());

		API api = apis.get(0);
		ApiEndpoint endpoint = api.getApiEndpoint();

		assertEquals("POST", endpoint.getMethod());
		assertEquals("/api/test/{id}", endpoint.getApi());

		List<String> requiredFiles = api.getPaths();

		// 의존성 파일들이 포함되어 있는지 확인
		assertTrue(requiredFiles.contains(filePath));
		assertTrue(requiredFiles.contains("/path/to/MyRequestDto.java"));
		assertTrue(requiredFiles.contains("/path/to/MyResponseDto.java"));

		// dependencyExplorer.findClassDependencies가 예상된 클래스 이름으로 호출되었는지 확인
		verify(dependencyExplorer).findClassDependencies(eq("MyControllerWithParams"), anySet(), anySet());
		verify(dependencyExplorer).findClassDependencies(eq("MyRequestDto"), anySet(), anySet());
		verify(dependencyExplorer).findClassDependencies(eq("MyResponseDto"), anySet(), anySet());
	}

	@Test
	public void testFindDependency_noMethods() throws Exception {
		// 준비
		String className = "com.example.EmptyController";
		String fileName = "EmptyController.java";
		String filePath = tempDir.resolve(fileName).toString();

		Map<String, String> classToFilePathMap = new HashMap<>();
		classToFilePathMap.put(className, filePath);
		when(javaClassifiedDataContainer.getClassToFilePath()).thenReturn(classToFilePathMap);

		String srcContent =
			"package com.example;\n"
				+ "import org.springframework.web.bind.annotation.RestController;\n"
				+ "\n"
				+ "@RestController\n"
				+ "public class EmptyController {\n"
				+ "}";

		Files.writeString(tempDir.resolve(fileName), srcContent);

		// 실행
		List<API> apis = dependencyAnalyzer.findDependency(className);

		// 검증
		assertNotNull(apis);
		assertEquals(0, apis.size());
	}

	@Test
	public void testFindDependency_nullFilePath() throws Exception {
		// 준비
		String className = "com.example.UnknownController";
		when(javaClassifiedDataContainer.getClassToFilePath()).thenReturn(new HashMap<>());

		// 실행
		List<API> apis = dependencyAnalyzer.findDependency(className);

		// 검증
		assertNull(apis);
	}
}
