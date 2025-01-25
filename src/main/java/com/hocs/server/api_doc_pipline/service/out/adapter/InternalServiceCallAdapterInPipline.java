package com.hocs.server.api_doc_pipline.service.out.adapter;

import com.hocs.server.api_doc_pipline.domain.ApiInfoInPipline;
import com.hocs.server.api_doc_pipline.domain.ControllerFile;
import com.hocs.server.api_doc_pipline.service.out.port.ApiEndpointCollectorPortInPipline;
import com.hocs.server.code_resolver.facade.ApiEndpointResolveFacade;
import com.hocs.server.common.CodingLanguage;
import com.hocs.server.common.ProjectFramework;
import com.hocs.server.common.ProjectMetaData;
import com.hocs.server.openai.domain.input.APIMetadata;
import com.hocs.server.saas_v2.common.annotation.Adapter;
import com.hocs.server.saas_v2.domain.ApiInfo;
import com.hocs.server.common.ClientProjectPath;
import com.hocs.server.saas_v2.service.out.ApiEndpointCollector.adapter.FindApiInfoApiRequest;
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
		ClientProjectPath path, int firstPageSize) {

		FindApiInfoApiRequest request = new FindApiInfoApiRequest(language,
			projectFramework, path, firstPageSize);

		Map<ControllerFile, List<ApiInfo>> apiInfo = service.findApiInfo(request);

		return mapping(apiInfo);
	}

	@Override
	public List<APIMetadata> getApiEndpoints(String userId, ProjectMetaData metaData,
		String defaultBranchName, ControllerFile controllerFile) {

		return service.findAPIMetadata(userId,metaData,defaultBranchName,controllerFile);
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