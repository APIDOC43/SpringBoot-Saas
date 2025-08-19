package com.hocs.server.saas_platform.domain;



import com.hocs.server.code_parser.core.util.EndpointPathUtil;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.hocs.server.code_parser.core.domain.ApiEndpoint;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("EndpointPathUtil 테스트")
class EndpointPathUtilTest {

	private final JavaParser javaParser = new JavaParser();

	@Test
	@DisplayName("GetMapping 어노테이션에서 API 엔드포인트를 올바르게 추출해야 한다")
	void shouldExtractApiEndpointFromGetMapping() {
		// Given
		String code = """
			@RestController
			@RequestMapping("/api")
			public class TestController {
				@GetMapping("/users")
				public void getUsers() {}
			}
			""";

		CompilationUnit cu = javaParser.parse(code).getResult().orElseThrow();
		ClassOrInterfaceDeclaration clazz = cu.findFirst(ClassOrInterfaceDeclaration.class).orElseThrow();
		MethodDeclaration method = clazz.findFirst(MethodDeclaration.class).orElseThrow();

		// When
		ApiEndpoint result = EndpointPathUtil.generateApiEndpoint("/api", method);

		// Then
		assertThat(result.getApi()).isEqualTo("/api/users");
		assertThat(result.getMethod()).isEqualTo("GET");
	}

	@Test
	@DisplayName("PostMapping 어노테이션에서 API 엔드포인트를 올바르게 추출해야 한다")
	void shouldExtractApiEndpointFromPostMapping() {
		// Given
		String code = """
			@RestController
			public class TestController {
				@PostMapping("/create")
				public void createUser() {}
			}
			""";

		CompilationUnit cu = javaParser.parse(code).getResult().orElseThrow();
		ClassOrInterfaceDeclaration clazz = cu.findFirst(ClassOrInterfaceDeclaration.class).orElseThrow();
		MethodDeclaration method = clazz.findFirst(MethodDeclaration.class).orElseThrow();

		// When
		ApiEndpoint result = EndpointPathUtil.generateApiEndpoint("", method);

		// Then
		assertThat(result.getApi()).isEqualTo("/create");
		assertThat(result.getMethod()).isEqualTo("POST");
	}

	@Test
	@DisplayName("RequestMapping의 value 속성에서 API 엔드포인트를 올바르게 추출해야 한다")
	void shouldExtractApiEndpointFromRequestMappingWithValue() {
		// Given
		String code = """
			@RestController
			public class TestController {
				@RequestMapping(value = "/test", method = RequestMethod.PUT)
				public void updateUser() {}
			}
			""";

		CompilationUnit cu = javaParser.parse(code).getResult().orElseThrow();
		ClassOrInterfaceDeclaration clazz = cu.findFirst(ClassOrInterfaceDeclaration.class).orElseThrow();
		MethodDeclaration method = clazz.findFirst(MethodDeclaration.class).orElseThrow();

		// When
		ApiEndpoint result = EndpointPathUtil.generateApiEndpoint("/api", method);

		// Then
		assertThat(result.getApi()).isEqualTo("/api/test");
		assertThat(result.getMethod()).isEqualTo("PUT");
	}

	@Test
	@DisplayName("RequestMapping의 path 속성에서 API 엔드포인트를 올바르게 추출해야 한다")
	void shouldExtractApiEndpointFromRequestMappingWithPath() {
		// Given
		String code = """
			@RestController
			public class TestController {
				@RequestMapping(path = "/delete", method = RequestMethod.DELETE)
				public void deleteUser() {}
			}
			""";

		CompilationUnit cu = javaParser.parse(code).getResult().orElseThrow();
		ClassOrInterfaceDeclaration clazz = cu.findFirst(ClassOrInterfaceDeclaration.class).orElseThrow();
		MethodDeclaration method = clazz.findFirst(MethodDeclaration.class).orElseThrow();

		// When
		ApiEndpoint result = EndpointPathUtil.generateApiEndpoint("/api", method);

		// Then
		assertThat(result.getApi()).isEqualTo("/api/delete");
		assertThat(result.getMethod()).isEqualTo("DELETE");
	}

	@Test
	@DisplayName("클래스 수준의 RequestMapping에서 기본 경로를 올바르게 추출해야 한다")
	void shouldExtractBasePathFromClassLevelRequestMapping() {
		// Given
		String code = """
			@RestController
			@RequestMapping("/api/v1")
			public class TestController {
				public void someMethod() {}
			}
			""";

		CompilationUnit cu = javaParser.parse(code).getResult().orElseThrow();
		ClassOrInterfaceDeclaration clazz = cu.findFirst(ClassOrInterfaceDeclaration.class).orElseThrow();

		// When
		String result = EndpointPathUtil.findRequestMappingValue(clazz);

		// Then
		assertThat(result).isEqualTo("/api/v1");
	}

	@Test
	@DisplayName("RequestMapping이 없는 클래스에서는 빈 문자열을 반환해야 한다")
	void shouldReturnEmptyStringWhenNoRequestMapping() {
		// Given
		String code = """
			@RestController
			public class TestController {
				public void someMethod() {}
			}
			""";

		CompilationUnit cu = javaParser.parse(code).getResult().orElseThrow();
		ClassOrInterfaceDeclaration clazz = cu.findFirst(ClassOrInterfaceDeclaration.class).orElseThrow();

		// When
		String result = EndpointPathUtil.findRequestMappingValue(clazz);

		// Then
		assertThat(result).isEqualTo("");
	}

	@Test
	@DisplayName("기본 경로와 메서드 경로가 모두 슬래시로 끝나거나 시작할 때 올바르게 결합해야 한다")
	void shouldCombinePathsCorrectlyWithSlashes() {
		// Given
		String code = """
			@RestController
			public class TestController {
				@GetMapping("/users")
				public void getUsers() {}
			}
			""";

		CompilationUnit cu = javaParser.parse(code).getResult().orElseThrow();
		ClassOrInterfaceDeclaration clazz = cu.findFirst(ClassOrInterfaceDeclaration.class).orElseThrow();
		MethodDeclaration method = clazz.findFirst(MethodDeclaration.class).orElseThrow();

		// When
		ApiEndpoint result = EndpointPathUtil.generateApiEndpoint("/api/", method);

		// Then
		assertThat(result.getApi()).isEqualTo("/api/users");
	}

	@Test
	@DisplayName("기본 경로에 슬래시가 없고 메서드 경로에 슬래시가 없을 때 올바르게 결합해야 한다")
	void shouldCombinePathsWithoutSlashes() {
		// Given
		String code = """
			@RestController
			public class TestController {
				@GetMapping("users")
				public void getUsers() {}
			}
			""";

		CompilationUnit cu = javaParser.parse(code).getResult().orElseThrow();
		ClassOrInterfaceDeclaration clazz = cu.findFirst(ClassOrInterfaceDeclaration.class).orElseThrow();
		MethodDeclaration method = clazz.findFirst(MethodDeclaration.class).orElseThrow();

		// When
		ApiEndpoint result = EndpointPathUtil.generateApiEndpoint("api", method);

		// Then
		assertThat(result.getApi()).isEqualTo("api/users");
	}

	@Test
	@DisplayName("빈 기본 경로와 메서드 경로가 주어졌을 때 정상 처리해야 한다")
	void shouldHandleEmptyPaths() {
		// Given
		String code = """
			@RestController
			public class TestController {
				@GetMapping("")
				public void getRoot() {}
			}
			""";

		CompilationUnit cu = javaParser.parse(code).getResult().orElseThrow();
		ClassOrInterfaceDeclaration clazz = cu.findFirst(ClassOrInterfaceDeclaration.class).orElseThrow();
		MethodDeclaration method = clazz.findFirst(MethodDeclaration.class).orElseThrow();

		// When
		ApiEndpoint result = EndpointPathUtil.generateApiEndpoint("", method);

		// Then
		assertThat(result.getApi()).isEqualTo("");
		assertThat(result.getMethod()).isEqualTo("GET");
	}

	@Test
	@DisplayName("매핑 어노테이션이 없는 메서드에서는 빈 값을 반환해야 한다")
	void shouldReturnEmptyValuesWhenNoMappingAnnotation() {
		// Given
		String code = """
			@RestController
			public class TestController {
				public void normalMethod() {}
			}
			""";

		CompilationUnit cu = javaParser.parse(code).getResult().orElseThrow();
		ClassOrInterfaceDeclaration clazz = cu.findFirst(ClassOrInterfaceDeclaration.class).orElseThrow();
		MethodDeclaration method = clazz.findFirst(MethodDeclaration.class).orElseThrow();

		// When
		ApiEndpoint result = EndpointPathUtil.generateApiEndpoint("/api", method);

		// Then
		assertThat(result.getApi()).isEqualTo("/api");
		assertThat(result.getMethod()).isEqualTo("");
	}

	@Test
	@DisplayName("PatchMapping 어노테이션에서 API 엔드포인트를 올바르게 추출해야 한다")
	void shouldExtractApiEndpointFromPatchMapping() {
		// Given
		String code = """
			@RestController
			public class TestController {
				@PatchMapping("/users/{id}")
				public void patchUser() {}
			}
			""";

		CompilationUnit cu = javaParser.parse(code).getResult().orElseThrow();
		ClassOrInterfaceDeclaration clazz = cu.findFirst(ClassOrInterfaceDeclaration.class).orElseThrow();
		MethodDeclaration method = clazz.findFirst(MethodDeclaration.class).orElseThrow();

		// When
		ApiEndpoint result = EndpointPathUtil.generateApiEndpoint("/api", method);

		// Then
		assertThat(result.getApi()).isEqualTo("/api/users/{id}");
		assertThat(result.getMethod()).isEqualTo("PATCH");
	}

	@Test
	@DisplayName("DeleteMapping 어노테이션에서 API 엔드포인트를 올바르게 추출해야 한다")
	void shouldExtractApiEndpointFromDeleteMapping() {
		// Given
		String code = """
			@RestController
			public class TestController {
				@DeleteMapping("/users/{id}")
				public void deleteUser() {}
			}
			""";

		CompilationUnit cu = javaParser.parse(code).getResult().orElseThrow();
		ClassOrInterfaceDeclaration clazz = cu.findFirst(ClassOrInterfaceDeclaration.class).orElseThrow();
		MethodDeclaration method = clazz.findFirst(MethodDeclaration.class).orElseThrow();

		// When
		ApiEndpoint result = EndpointPathUtil.generateApiEndpoint("/api", method);

		// Then
		assertThat(result.getApi()).isEqualTo("/api/users/{id}");
		assertThat(result.getMethod()).isEqualTo("DELETE");
	}
}
