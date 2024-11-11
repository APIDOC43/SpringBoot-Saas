package com.hocs.server.openai.domain;



import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class APIEndpointTest {

	private static final String TEST_API = "TestAPI";
	private static final String TEST_METHOD = "GET";
	private static final List<String> TEST_PATHS = Arrays.asList("path/to/file1.java", "path/to/file2.java");
	private static final List<String> TEST_GLOBAL_SRC = Arrays.asList("globalSrc1", "globalSrc2");
	private static final String TEST_ABSOLUTE_PATH = "path/to/absolute";

	private APIEndpoint apiEndpoint;

	@TempDir
	Path tempDir;


	@Test
	void testCreate() {
		APIEndpoint endpoint = APIEndpoint.create(TEST_API, TEST_METHOD, TEST_PATHS, TEST_GLOBAL_SRC, TEST_ABSOLUTE_PATH);

		assertNotNull(endpoint);
		assertEquals(TEST_API, endpoint.getAPI());
		assertEquals(TEST_METHOD, endpoint.getMethod());
		assertEquals(TEST_PATHS, endpoint.getPaths());
		assertEquals(TEST_GLOBAL_SRC, endpoint.getGlobalSrc());
		assertEquals(TEST_ABSOLUTE_PATH, endpoint.getAbsolutePath());
	}

	@Test
	void testLoadSrc() throws IOException {
		// 임시 파일 생성 및 내용 작성
		Path tempFile1 = tempDir.resolve("file1.java");
		Path tempFile2 = tempDir.resolve("file2.java");
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile1.toFile()))) {
			writer.write("import java.util.List;\n");
			writer.write("public class TestClass1 {}\n");
		}
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile2.toFile()))) {
			writer.write("import java.io.File;\n");
			writer.write("public class TestClass2 {}\n");
		}

		List<String> paths = Arrays.asList(tempFile1.toString(), tempFile2.toString());
		apiEndpoint = APIEndpoint.create(TEST_API, TEST_METHOD, paths, TEST_GLOBAL_SRC, TEST_ABSOLUTE_PATH);
		String result = apiEndpoint.loadSrcString();

		// 예상 결과 검증
		String expected ="""
						 ### File: file1.java
						 ```java
						 public class TestClass1 {}
						 ```
						 
						 ### File: file2.java
						 ```java
						 public class TestClass2 {}
						 ```
						   
						 """;
		assertEquals(expected, result);
	}

	@Test
	void testLoadSrcWithInvalidPath() {
		// 존재하지 않는 파일 경로
		List<String> paths = Arrays.asList("invalid/path/file1.java");
		apiEndpoint = APIEndpoint.create(TEST_API, TEST_METHOD, paths, TEST_GLOBAL_SRC, TEST_ABSOLUTE_PATH);
		String result = apiEndpoint.loadSrcString();

		// 결과가 빈 문자열인지 확인 (예외 상황 처리)
		assertEquals("", result);
	}
}
