package com.hocs.server.extractor.core.util;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import java.util.Arrays;
import java.util.List;

public class HttpMethodManager {
	/**
	 * 어노테이션 이름과 내용을 기반으로 HTTP 메서드를 추출합니다.
	 * @param annotationName 어노테이션 이름 (e.g., GetMapping)
	 * @param annotation 어노테이션 객체
	 * @return HTTP 메서드 이름 (e.g., GET)
	 */
	public static String extractHttpMethod(String annotationName, AnnotationExpr annotation) {
		switch (annotationName) {
			case "GetMapping":
				return "GET";
			case "PostMapping":
				return "POST";
			case "PutMapping":
				return "PUT";
			case "DeleteMapping":
				return "DELETE";
			case "PatchMapping":
				return "PATCH";
			case "RequestMapping":
				// @RequestMapping의 'method' 속성에서 HTTP 메서드를 추출
				if (annotation.isNormalAnnotationExpr()) {
					return annotation.asNormalAnnotationExpr().getPairs().stream()
						.filter(pair -> pair.getNameAsString().equals("method"))
						.map(pair -> pair.getValue().toString().replaceAll("RequestMethod\\.", ""))
						.findFirst()
						.orElse("REQUEST");
				}
				return "REQUEST";
			default:
				return "UNKNOWN";
		}
	}

	/**
	 * HTTP 메서드 어노테이션인지 확인합니다.
	 */
	public static boolean haveHttpMethodAnnotation(MethodDeclaration method) {
		List<String> httpAnnotations = Arrays.asList(
			"GetMapping", "PostMapping", "PutMapping", "DeleteMapping", "PatchMapping",
			"RequestMapping"
		);
		for (AnnotationExpr annotation : method.getAnnotations()) {
			String annotationName = annotation.getNameAsString();
			if (httpAnnotations.contains(annotationName)) {
				return true;
			}
		}
		return false;
	}

}
