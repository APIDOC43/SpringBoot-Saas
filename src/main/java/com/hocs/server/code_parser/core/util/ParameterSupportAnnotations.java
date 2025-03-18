package com.hocs.server.code_parser.core.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;


public class ParameterSupportAnnotations {
	// 지원하는 파라미터 어노테이션 목록 생성
	public static final Set<String> supportedAnnotations = new HashSet<>(Arrays.asList(
		"RequestBody",
		"RequestParam",
		"PathVariable",
		"RequestHeader",
		"CookieValue",
		"SessionAttribute",
		"ModelAttribute",
		"RequestPart",
		"MatrixVariable",
		"RequestAttribute"
	));

}
