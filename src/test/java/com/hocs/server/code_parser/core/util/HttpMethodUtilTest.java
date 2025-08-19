package com.hocs.server.code_parser.core.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;

@DisplayName("HttpMethodUtil 테스트")
class HttpMethodUtilTest {

	private final JavaParser javaParser = new JavaParser();

	@Test
	@DisplayName("GetMapping 어노테이션에서 GET 메서드를 추출해야 한다")
	void shouldExtractGetMethodFromGetMapping() {
		// Given
		String code = """
			@RestController
			public class TestController {
				@GetMapping("/users")
				public void getUsers() {}
			}
			""";

		AnnotationExpr annotation = getFirstAnnotationFromMethod(code);

		// When
		String result = HttpMethodUtil.extractHttpMethod("GetMapping", annotation);

		// Then
		assertThat(result).isEqualTo("GET");
	}

	@Test
	@DisplayName("PostMapping 어노테이션에서 POST 메서드를 추출해야 한다")
	void shouldExtractPostMethodFromPostMapping() {
		// Given
		String code = """
			@RestController
			public class TestController {
				@PostMapping("/users")
				public void createUser() {}
			}
			""";

		AnnotationExpr annotation = getFirstAnnotationFromMethod(code);

		// When
		String result = HttpMethodUtil.extractHttpMethod("PostMapping", annotation);

		// Then
		assertThat(result).isEqualTo("POST");
	}

	@Test
	@DisplayName("PutMapping 어노테이션에서 PUT 메서드를 추출해야 한다")
	void shouldExtractPutMethodFromPutMapping() {
		// Given
		String code = """
			@RestController
			public class TestController {
				@PutMapping("/users/{id}")
				public void updateUser() {}
			}
			""";

		AnnotationExpr annotation = getFirstAnnotationFromMethod(code);

		// When
		String result = HttpMethodUtil.extractHttpMethod("PutMapping", annotation);

		// Then
		assertThat(result).isEqualTo("PUT");
	}

	@Test
	@DisplayName("DeleteMapping 어노테이션에서 DELETE 메서드를 추출해야 한다")
	void shouldExtractDeleteMethodFromDeleteMapping() {
		// Given
		String code = """
			@RestController
			public class TestController {
				@DeleteMapping("/users/{id}")
				public void deleteUser() {}
			}
			""";

		AnnotationExpr annotation = getFirstAnnotationFromMethod(code);

		// When
		String result = HttpMethodUtil.extractHttpMethod("DeleteMapping", annotation);

		// Then
		assertThat(result).isEqualTo("DELETE");
	}

	@Test
	@DisplayName("PatchMapping 어노테이션에서 PATCH 메서드를 추출해야 한다")
	void shouldExtractPatchMethodFromPatchMapping() {
		// Given
		String code = """
			@RestController
			public class TestController {
				@PatchMapping("/users/{id}")
				public void patchUser() {}
			}
			""";

		AnnotationExpr annotation = getFirstAnnotationFromMethod(code);

		// When
		String result = HttpMethodUtil.extractHttpMethod("PatchMapping", annotation);

		// Then
		assertThat(result).isEqualTo("PATCH");
	}

	@Test
	@DisplayName("RequestMapping 어노테이션의 method 속성에서 HTTP 메서드를 추출해야 한다")
	void shouldExtractHttpMethodFromRequestMappingMethod() {
		// Given
		String code = """
			@RestController
			public class TestController {
				@RequestMapping(value = "/users", method = RequestMethod.POST)
				public void createUser() {}
			}
			""";

		AnnotationExpr annotation = getFirstAnnotationFromMethod(code);

		// When
		String result = HttpMethodUtil.extractHttpMethod("RequestMapping", annotation);

		// Then
		assertThat(result).isEqualTo("POST");
	}

	@Test
	@DisplayName("RequestMapping 어노테이션에 method 속성이 없으면 REQUEST를 반환해야 한다")
	void shouldReturnRequestWhenNoMethodInRequestMapping() {
		// Given
		String code = """
			@RestController
			public class TestController {
				@RequestMapping("/users")
				public void handleUsers() {}
			}
			""";

		AnnotationExpr annotation = getFirstAnnotationFromMethod(code);

		// When
		String result = HttpMethodUtil.extractHttpMethod("RequestMapping", annotation);

		// Then
		assertThat(result).isEqualTo("REQUEST");
	}

	@Test
	@DisplayName("알 수 없는 어노테이션에서는 UNKNOWN을 반환해야 한다")
	void shouldReturnUnknownForUnknownAnnotation() {
		// Given
		String code = """
			@RestController
			public class TestController {
				@CustomMapping("/users")
				public void customMethod() {}
			}
			""";

		AnnotationExpr annotation = getFirstAnnotationFromMethod(code);

		// When
		String result = HttpMethodUtil.extractHttpMethod("CustomMapping", annotation);

		// Then
		assertThat(result).isEqualTo("UNKNOWN");
	}

	@Test
	@DisplayName("HTTP 메서드 어노테이션이 있는 메서드를 올바르게 식별해야 한다")
	void shouldIdentifyMethodWithHttpAnnotation() {
		// Given
		String code = """
			@RestController
			public class TestController {
				@GetMapping("/users")
				public void getUsers() {}
			}
			""";

		MethodDeclaration method = getMethodFromCode(code);

		// When
		boolean result = HttpMethodUtil.haveHttpMethodAnnotation(method);

		// Then
		assertThat(result).isTrue();
	}

	@Test
	@DisplayName("HTTP 메서드 어노테이션이 없는 메서드를 올바르게 식별해야 한다")
	void shouldIdentifyMethodWithoutHttpAnnotation() {
		// Given
		String code = """
			@RestController
			public class TestController {
				@Service
				public void normalMethod() {}
			}
			""";

		MethodDeclaration method = getMethodFromCode(code);

		// When
		boolean result = HttpMethodUtil.haveHttpMethodAnnotation(method);

		// Then
		assertThat(result).isFalse();
	}

	@Test
	@DisplayName("어노테이션이 없는 메서드를 올바르게 식별해야 한다")
	void shouldIdentifyMethodWithNoAnnotations() {
		// Given
		String code = """
			@RestController
			public class TestController {
				public void plainMethod() {}
			}
			""";

		MethodDeclaration method = getMethodFromCode(code);

		// When
		boolean result = HttpMethodUtil.haveHttpMethodAnnotation(method);

		// Then
		assertThat(result).isFalse();
	}

	@Test
	@DisplayName("RequestMapping 어노테이션을 HTTP 메서드 어노테이션으로 식별해야 한다")
	void shouldIdentifyRequestMappingAsHttpAnnotation() {
		// Given
		String code = """
			@RestController
			public class TestController {
				@RequestMapping(value = "/users", method = RequestMethod.GET)
				public void getUsers() {}
			}
			""";

		MethodDeclaration method = getMethodFromCode(code);

		// When
		boolean result = HttpMethodUtil.haveHttpMethodAnnotation(method);

		// Then
		assertThat(result).isTrue();
	}

	@Test
	@DisplayName("여러 어노테이션 중 HTTP 메서드 어노테이션이 있으면 true를 반환해야 한다")
	void shouldReturnTrueWhenHttpAnnotationExistsAmongMultiple() {
		// Given
		String code = """
			@RestController
			public class TestController {
				@Transactional
				@GetMapping("/users")
				@Valid
				public void getUsers() {}
			}
			""";

		MethodDeclaration method = getMethodFromCode(code);

		// When
		boolean result = HttpMethodUtil.haveHttpMethodAnnotation(method);

		// Then
		assertThat(result).isTrue();
	}

	private AnnotationExpr getFirstAnnotationFromMethod(String code) {
		CompilationUnit cu = javaParser.parse(code).getResult().orElseThrow();
		ClassOrInterfaceDeclaration clazz = cu.findFirst(ClassOrInterfaceDeclaration.class).orElseThrow();
		MethodDeclaration method = clazz.findFirst(MethodDeclaration.class).orElseThrow();
		return method.getAnnotations().get(0);
	}

	private MethodDeclaration getMethodFromCode(String code) {
		CompilationUnit cu = javaParser.parse(code).getResult().orElseThrow();
		ClassOrInterfaceDeclaration clazz = cu.findFirst(ClassOrInterfaceDeclaration.class).orElseThrow();
		return clazz.findFirst(MethodDeclaration.class).orElseThrow();
	}
}
