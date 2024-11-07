package com.hocs.server.extractor.util;

import static org.assertj.core.api.Assertions.assertThat;

import com.hocs.server.extractor.domain.API;
import com.hocs.server.extractor.domain.APISourceDependencyInfo;
import com.hocs.server.extractor.domain.GlobalSourceDependency;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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

	@Test
	@DisplayName("List<Map<String, Object>>를 APISourceDependencyInfo로 매핑 테스트")
	public void testMapToAPISourceDependencyInfo() {
		APISourceDependencyInfo result = APISourceDependencyInfoMapper.mapToAPISourceDependencyInfo(
			testData, testUserId);

		// 검증: userId
		assertThat(result.getUserId()).isEqualTo(testUserId);

		// 검증: API
		List<API> apis = result.getApiSourceDependencies();
		assertThat(apis).hasSize(1);
		assertThat(apis.get(0).getApi()).isEqualTo("/product/search/");
		assertThat(apis.get(0).getMethod()).isEqualTo("POST");
		assertThat(apis.get(0).getPaths()).contains("/path/to/DataResponse.java",
			"/path/to/ProductSearchService.java");

		// 검증: GlobalSourceDependency
		GlobalSourceDependency global = result.getGlobal();
		assertThat(global.getAop()).isNotNull();
		assertThat(global.getAop().getPaths()).contains("/path/to/LoginCheckAspect.java");

		assertThat(global.getExceptionHandler()).isNotNull();
		assertThat(global.getExceptionHandler().getPaths()).contains(
			"/path/to/GlobalExceptionHandler.java");

		assertThat(global.getConfiguration()).contains("/path/to/JpaAuditingConfig.java",
			"/path/to/RedisConfig.java");
		assertThat(global.getComponent()).contains("/path/to/Component.java");
	}
}