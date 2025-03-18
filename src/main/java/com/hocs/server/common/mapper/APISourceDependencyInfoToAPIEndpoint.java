package com.hocs.server.common.mapper;

import com.hocs.server.api_spec_generator.domain.input.APIMetadata;
import com.hocs.server.code_parser.core.domain.API;
import com.hocs.server.code_parser.core.domain.APISourceDependencyInfo;
import com.hocs.server.code_parser.core.domain.ApiEndpoint;
import com.hocs.server.code_parser.core.domain.GlobalSourceDependency;
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
