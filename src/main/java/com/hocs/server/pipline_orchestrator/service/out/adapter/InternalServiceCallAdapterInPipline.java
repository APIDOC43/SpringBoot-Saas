package com.hocs.server.pipline_orchestrator.service.out.adapter;

import com.hocs.server.api_spec_generator.domain.input.APIMetadata;
import com.hocs.server.code_parser.core.domain.APISourceDependencyInfo;
import com.hocs.server.code_parser.facade.ApiEndpointResolveFacade;
import com.hocs.server.common.domain.ApiInfo;
import com.hocs.server.common.domain.ClientProjectPath;
import com.hocs.server.common.domain.CodingLanguage;
import com.hocs.server.common.domain.ProjectFramework;
import com.hocs.server.common.domain.ProjectMetaData;
import com.hocs.server.common.mapper.APISourceDependencyInfoToAPIEndpoint;
import com.hocs.server.pipline_orchestrator.domain.ApiInfoInPipline;
import com.hocs.server.pipline_orchestrator.domain.ControllerFile;
import com.hocs.server.pipline_orchestrator.service.out.port.ApiEndpointCollectorPortInPipline;
import com.hocs.server.saas_platform.common.annotation.Adapter;
import com.hocs.server.saas_platform.service.external.ApiEndpointCollector.adapter.FindApiInfoApiRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;


@Adapter
@RequiredArgsConstructor
public class InternalServiceCallAdapterInPipline implements ApiEndpointCollectorPortInPipline {

	private final ApiEndpointResolveFacade service;

	@Override
	public Map<ControllerFile, List<ApiInfoInPipline>> findApiInfo(CodingLanguage language,
		ProjectFramework projectFramework,
		ClientProjectPath path, List<ApiInfo> excludeApiInfo, int firstPageSize) {

		FindApiInfoApiRequest request = new FindApiInfoApiRequest(language,
			projectFramework, path, firstPageSize,excludeApiInfo);

		Map<ControllerFile, List<ApiInfo>> apiInfo = service.findApiInfo(request);

		return mapping(apiInfo);
	}

	@Override
	public List<APIMetadata> getApiEndpoints(String userId, ProjectMetaData metaData,
		String defaultBranchName, ControllerFile controllerFile, String requestId) {

		APISourceDependencyInfo apiSourceDependencyInfo = service.findAPIMetadata(userId, metaData,
			defaultBranchName, controllerFile, requestId);

		return APISourceDependencyInfoToAPIEndpoint.mapToAPIEndpoint(apiSourceDependencyInfo);
	}

	private static HashMap<ControllerFile, List<ApiInfoInPipline>> mapping(
		Map<ControllerFile, List<ApiInfo>> apiInfo) {
		HashMap<ControllerFile, List<ApiInfoInPipline>> converted = new HashMap<>();
		for (ControllerFile controllerFile : apiInfo.keySet()) {
			List<ApiInfo> apiInfos = apiInfo.get(controllerFile);
			List<ApiInfoInPipline> convertedValue = apiInfos.stream()
				.map(m -> new ApiInfoInPipline(m.getHttpMethod(), m.getEndpoint(),
					m.getMethodSignature()))
				.collect(Collectors.toList());

			converted.put(controllerFile, convertedValue);
		}
		return converted;
	}
}