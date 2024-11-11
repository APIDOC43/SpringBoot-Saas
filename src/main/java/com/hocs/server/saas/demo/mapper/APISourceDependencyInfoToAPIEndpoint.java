package com.hocs.server.saas.demo.mapper;

import com.hocs.server.extractor.domain.API;
import com.hocs.server.extractor.domain.APISourceDependencyInfo;
import com.hocs.server.extractor.domain.ApiEndpoint;
import com.hocs.server.extractor.domain.GlobalSourceDependency;
import com.hocs.server.openai.domain.APIEndpoint;
import java.util.ArrayList;
import java.util.List;

public class APISourceDependencyInfoToAPIEndpoint {

	public static List<APIEndpoint> mapToAPIEndpoint(APISourceDependencyInfo sourceDependencyInfo) {
		List<APIEndpoint> apiEntries = new ArrayList<>();

		GlobalSourceDependency global = sourceDependencyInfo.getGlobal();

		if (sourceDependencyInfo != null && sourceDependencyInfo.getApiSourceDependencies() != null) {
			for (API api : sourceDependencyInfo.getApiSourceDependencies()) {
				ApiEndpoint apiEndpoint = api.getApiEndpoint();
				String apiPath = apiEndpoint != null ? apiEndpoint.getApi() : "unknown";
				String method = apiEndpoint != null ? apiEndpoint.getMethod() : "unknown";

				List<String> paths = api.getPaths();
				String absolutePath = api.getLink() != null ? api.getLink() : "unknown";
				// Map fields to APIEndpoint
				APIEndpoint apiEntry = APIEndpoint.create(apiPath, method, paths, global.getAllSourcePathList(),absolutePath);
				apiEntries.add(apiEntry);
			}
		}

		return apiEntries;
	}
}
