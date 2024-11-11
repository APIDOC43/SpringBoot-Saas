package com.hocs.server.saas.demo.mapper;

import com.hocs.server.extractor.domain.API;
import com.hocs.server.extractor.domain.APISourceDependencyInfo;
import com.hocs.server.extractor.domain.ApiEndpoint;
import com.hocs.server.openai.domain.APIEndpoint;
import java.util.ArrayList;
import java.util.List;

public class APISourceDependencyInfoToAPIEntryMapper {

	public static List<APIEndpoint> mapToAPIEntries(APISourceDependencyInfo sourceDependencyInfo) {
		List<APIEndpoint> apiEntries = new ArrayList<>();

		if (sourceDependencyInfo != null && sourceDependencyInfo.getApiSourceDependencies() != null) {
			for (API api : sourceDependencyInfo.getApiSourceDependencies()) {
				ApiEndpoint apiEndpoint = api.getApiEndpoint();
				String apiPath = apiEndpoint != null ? apiEndpoint.getApi() : "unknown";
				String method = apiEndpoint != null ? apiEndpoint.getMethod() : "unknown";

				List<String> paths = api.getPaths();
				String absolutePath = api.getLink() != null ? api.getLink() : "unknown";

				// Map fields to APIEndpoint
				APIEndpoint apiEntry = new APIEndpoint(apiPath, method, paths, absolutePath);
				apiEntries.add(apiEntry);
			}
		}

		return apiEntries;
	}
}
