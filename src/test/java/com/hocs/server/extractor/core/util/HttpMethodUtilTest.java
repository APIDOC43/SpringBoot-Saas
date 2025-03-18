package com.hocs.server.extractor.core.util;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.*;
import com.hocs.server.code_parser.core.util.HttpMethodUtil;
import org.junit.jupiter.api.Test;

public class HttpMethodUtilTest {

	@Test
	public void testExtractHttpMethod() {
		// 테스트를 위한 AnnotationExpr 생성
		AnnotationExpr getMapping = new MarkerAnnotationExpr("GetMapping");
		AnnotationExpr postMapping = new MarkerAnnotationExpr("PostMapping");
		AnnotationExpr putMapping = new MarkerAnnotationExpr("PutMapping");
		AnnotationExpr deleteMapping = new MarkerAnnotationExpr("DeleteMapping");
		AnnotationExpr patchMapping = new MarkerAnnotationExpr("PatchMapping");
		AnnotationExpr unknownMapping = new MarkerAnnotationExpr("UnknownMapping");

		// @RequestMapping(method = RequestMethod.GET) 형태의 어노테이션 생성
		NormalAnnotationExpr requestMapping = new NormalAnnotationExpr();
		requestMapping.setName("RequestMapping");
		requestMapping.addPair("method", new NameExpr("RequestMethod.GET"));

		// @RequestMapping 어노테이션 (method 속성 없음)
		NormalAnnotationExpr requestMappingWithoutMethod = new NormalAnnotationExpr();
		requestMappingWithoutMethod.setName("RequestMapping");

		// 각 어노테이션에 대한 HTTP 메서드 추출 테스트
		assertEquals("GET", HttpMethodUtil.extractHttpMethod("GetMapping", getMapping));
		assertEquals("POST", HttpMethodUtil.extractHttpMethod("PostMapping", postMapping));
		assertEquals("PUT", HttpMethodUtil.extractHttpMethod("PutMapping", putMapping));
		assertEquals("DELETE", HttpMethodUtil.extractHttpMethod("DeleteMapping", deleteMapping));
		assertEquals("PATCH", HttpMethodUtil.extractHttpMethod("PatchMapping", patchMapping));
		assertEquals("UNKNOWN", HttpMethodUtil.extractHttpMethod("UnknownMapping", unknownMapping));

		// @RequestMapping(method = RequestMethod.GET)
		assertEquals("GET", HttpMethodUtil.extractHttpMethod("RequestMapping", requestMapping));

		// @RequestMapping (method 속성 없음)
		assertEquals("REQUEST", HttpMethodUtil.extractHttpMethod("RequestMapping", requestMappingWithoutMethod));
	}

	@Test
	public void testHaveHttpMethodAnnotation() {
		// 메서드 선언 생성
		MethodDeclaration methodWithHttpAnnotation = new MethodDeclaration();
		methodWithHttpAnnotation.addAnnotation(new MarkerAnnotationExpr("GetMapping"));

		MethodDeclaration methodWithoutHttpAnnotation = new MethodDeclaration();
		methodWithoutHttpAnnotation.addAnnotation(new MarkerAnnotationExpr("SomeOtherAnnotation"));

		// HTTP 메서드 어노테이션이 있는 경우
		assertTrue(HttpMethodUtil.haveHttpMethodAnnotation(methodWithHttpAnnotation));

		// HTTP 메서드 어노테이션이 없는 경우
		assertFalse(HttpMethodUtil.haveHttpMethodAnnotation(methodWithoutHttpAnnotation));
	}
}
