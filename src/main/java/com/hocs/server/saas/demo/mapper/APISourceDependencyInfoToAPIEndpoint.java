package com.hocs.server.saas.demo.mapper;

import com.hocs.server.code_resolver.legacy.extractor.domain.API;
import com.hocs.server.code_resolver.legacy.extractor.domain.APISourceDependencyInfo;
import com.hocs.server.code_resolver.legacy.extractor.domain.ApiEndpoint;
import com.hocs.server.code_resolver.legacy.extractor.domain.GlobalSourceDependency;
import com.hocs.server.openai.domain.input.APIMetadata;
import java.util.ArrayList;
import java.util.List;

public class APISourceDependencyInfoToAPIEndpoint {

	public static List<APIMetadata> mapToAPIEndpoint(APISourceDependencyInfo sourceDependencyInfo) {
		List<APIMetadata> apiEntries = new ArrayList<>();

		GlobalSourceDependency global = sourceDependencyInfo.getGlobal();

		if (sourceDependencyInfo != null && sourceDependencyInfo.getApiSourceDependencies() != null) {
			for (API api : sourceDependencyInfo.getApiSourceDependencies()) {
				ApiEndpoint apiEndpoint = api.getApiEndpoint();
				String apiPath = apiEndpoint != null ? apiEndpoint.getApi() : "unknown";
				String method = apiEndpoint != null ? apiEndpoint.getMethod() : "unknown";

				List<String> paths = api.getPaths();
				String absolutePath = api.getLink() != null ? api.getLink() : "unknown";
				// Map fields to APIMetadata
				APIMetadata apiEntry = APIMetadata.create(apiPath, method, paths, global.getAllSourcePathList(),absolutePath);
				apiEntries.add(apiEntry);
			}
		}

		return apiEntries;
	}
}
