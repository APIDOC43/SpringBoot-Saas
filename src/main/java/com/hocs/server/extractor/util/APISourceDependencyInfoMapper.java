package com.hocs.server.extractor.util;

import com.hocs.server.extractor.domain.AOP;
import com.hocs.server.extractor.domain.API;
import com.hocs.server.extractor.domain.APISourceDependencyInfo;
import com.hocs.server.extractor.domain.ExceptionHandler;
import com.hocs.server.extractor.domain.GlobalSourceDependency;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class APISourceDependencyInfoMapper {

	public static APISourceDependencyInfo mapToAPISourceDependencyInfo(List<Map<String, Object>> data, String userId) {
		List<API> apiList = new ArrayList<>();
		AOP aop = null;
		ExceptionHandler exceptionHandler = null;
		List<String> configuration = new ArrayList<>();
		List<String> component = new ArrayList<>();

		for (Map<String, Object> entry : data) {
			if (entry.containsKey("API")) {
				// API 매핑
				String api = (String) entry.get("API");
				String method = (String) entry.get("method");
				List<String> paths = (List<String>) entry.get("paths");

				API apiObj = API.create(api, method, paths);
				apiList.add(apiObj);

			} else if (entry.containsKey("Global")) {
				// Global 매핑
				Map<String, Object> globalMap = (Map<String, Object>) entry.get("Global");

				// AOP 매핑
				if (globalMap.containsKey("AOP")) {
					List<String> aopPaths = (List<String>) globalMap.get("AOP");
					aop = AOP.create(aopPaths);
				}

				// ExceptionHandler 매핑
				if (globalMap.containsKey("ExceptionHandler")) {
					List<String> exceptionPaths = (List<String>) globalMap.get("ExceptionHandler");
					exceptionHandler = ExceptionHandler.create("exceptionHandlerId", exceptionPaths);  // 필요한 경우 ID 할당
				}

				// Configuration 매핑
				if (globalMap.containsKey("Configuration")) {
					configuration = (List<String>) globalMap.get("Configuration");
				}

				// Component 매핑
				if (globalMap.containsKey("Component")) {
					component = (List<String>) globalMap.get("Component");
				}
			}
		}

		// GlobalSourceDependency 객체 생성
		GlobalSourceDependency globalSourceDependency = GlobalSourceDependency.create("globalDependencyId", aop, exceptionHandler, configuration, component);

		// 최상위 객체 APISourceDependencyInfo 생성
		return APISourceDependencyInfo.create("apiSourceDependencyInfoId", userId, apiList, globalSourceDependency);
	}
}
