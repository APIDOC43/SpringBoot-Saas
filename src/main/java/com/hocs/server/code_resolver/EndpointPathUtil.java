package com.hocs.server.code_resolver;

import static com.hocs.server.extractor.core.util.HttpMethodUtil.extractHttpMethod;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.hocs.server.extractor.domain.ApiEndpoint;
import java.util.concurrent.atomic.AtomicReference;

/**
 *  API Endpoint에 관한 책임을 가지는 클래스입니다.
 */
public class EndpointPathUtil {


	/**
	 * 메서드에 선언된 @RequestMapping 어노테이션에서 HTTP 메서드와 경로를 추출합니다.
	 * @ex) GetMapping("/sign-up") -> httpMethod : "GET", methodPath
	 */
	public static ApiEndpoint generateApiEndpoint(String basePath, MethodDeclaration method) {

		String methodPath = "";
		String httpMethod = "";
		for (AnnotationExpr annotation : method.getAnnotations()) {
			String annotationName = annotation.getNameAsString();
			if (annotationName.endsWith("Mapping")) {
				httpMethod = extractHttpMethod(annotationName, annotation);
				methodPath = extractPathOrValueFromAnnotation(annotation);
				break;
			}
		}

		String fullApiPath = combinePaths(basePath, methodPath);
		return ApiEndpoint.create(fullApiPath,httpMethod);
	}


	/**
	 * 클래스 수준의 @RequestMapping 어노테이션에서 경로(Path)를 추출합니다.
	 */
	public static String findRequestMappingValue(TypeDeclaration<?> typeDecl) {
		AtomicReference<String> basePath = new AtomicReference<>("");
		if (typeDecl.isAnnotationPresent("RequestMapping")) {
			typeDecl.getAnnotationByName("RequestMapping")
				.ifPresent(requestMapping -> basePath.set(
					extractPathOrValueFromAnnotation(requestMapping)));
		}
		return basePath.get();
	}

	/**
	 * 어노테이션의 path또는 value값을 추출합니다.
	 */
	private static String extractPathOrValueFromAnnotation(AnnotationExpr annotation) {
		if (annotation.isSingleMemberAnnotationExpr()) {
			return annotation.asSingleMemberAnnotationExpr().getMemberValue().toString().replaceAll("\"", "");
		} else if (annotation.isNormalAnnotationExpr()) {
			// 'value' 또는 'path' 속성에서 경로를 추출
			return annotation.asNormalAnnotationExpr().getPairs().stream()
				.filter(pair -> pair.getNameAsString().equals("value") || pair.getNameAsString().equals("path"))
				.map(pair -> pair.getValue().toString().replaceAll("\"", ""))
				.findFirst()
				.orElse("");
		}
		return "";
	}

	/**
	 * 두 개의 경로를 결합하여 전체 API 경로를 생성합니다.
	 * @basePath : "/store-owner" + methodPath : "/sign-up"
	 * @Retrun : "/store-owner/sign-up"
	 */
	private static String combinePaths(String basePath, String methodPath) {
		if (basePath == null) basePath = "";
		if (methodPath == null) methodPath = "";

		if (!basePath.endsWith("/") && !methodPath.startsWith("/")) {
			return basePath + "/" + methodPath;
		} else if (basePath.endsWith("/") && methodPath.startsWith("/")) {
			return basePath + methodPath.substring(1);
		} else {
			return basePath + methodPath;
		}
	}
}