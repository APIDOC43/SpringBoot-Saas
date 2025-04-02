package com.hocs.server.code_parser.core.dataobject;


import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class JavaClassifiedStore {
	private static final ConcurrentMap<String, JavaClassifiedDataContainer> CONTEXT_MAP = new ConcurrentHashMap<>();

	// 등록
	public static void put(String requestId, JavaClassifiedDataContainer context) {
		CONTEXT_MAP.put(requestId, context);
	}

	// 조회
	public static JavaClassifiedDataContainer get(String requestId) {
		return CONTEXT_MAP.get(requestId);
	}

	// 삭제
	public static void remove(String requestId) {
		CONTEXT_MAP.remove(requestId);
	}

	// 존재 확인 (선택적)
	public static boolean contains(String requestId) {
		return CONTEXT_MAP.containsKey(requestId);
	}
}