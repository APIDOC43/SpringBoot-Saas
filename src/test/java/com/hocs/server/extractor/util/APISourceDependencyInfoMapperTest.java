package com.hocs.server.extractor.util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;

public class APISourceDependencyInfoMapperTest {

	private List<Map<String, Object>> testData;
	private String testUserId = "testUser";

	@BeforeEach
	public void setUp() {
		// API 데이터 설정
		Map<String, Object> apiData = new HashMap<>();
		apiData.put("API", "/product/search/");
		apiData.put("method", "POST");
		apiData.put("paths",
			Arrays.asList("/path/to/DataResponse.java", "/path/to/ProductSearchService.java"));

		// Global 데이터 설정
		Map<String, Object> globalData = new HashMap<>();
		Map<String, Object> aopData = new HashMap<>();
		aopData.put("paths", List.of("/path/to/LoginCheckAspect.java"));

		Map<String, Object> exceptionHandlerData = new HashMap<>();
		exceptionHandlerData.put("paths", List.of("/path/to/GlobalExceptionHandler.java"));

		globalData.put("AOP", aopData.get("paths"));
		globalData.put("ExceptionHandler", exceptionHandlerData.get("paths"));
		globalData.put("Configuration",
			Arrays.asList("/path/to/JpaAuditingConfig.java", "/path/to/RedisConfig.java"));
		globalData.put("Component", List.of("/path/to/Component.java"));

		Map<String, Object> globalEntry = new HashMap<>();
		globalEntry.put("Global", globalData);

		// Test 데이터 구성
		testData = Arrays.asList(apiData, globalEntry);
	}

}