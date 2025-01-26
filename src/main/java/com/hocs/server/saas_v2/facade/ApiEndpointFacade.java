package com.hocs.server.saas_v2.facade;

import com.hocs.server.api_doc_pipline.domain.ControllerFile;
import com.hocs.server.saas_v2.api.request.FindApiInfoClientRequest;
import com.hocs.server.saas_v2.api.response.ApiInfoResponse;
import com.hocs.server.saas_v2.service.GitCloneService;
import com.hocs.server.saas_v2.service.ProjectMetaDataService;
import com.hocs.server.saas_v2.service.out.ApiEndpointCollector.port.ApiEndpointCollectorPort;
import com.hocs.server.saas_v2.common.annotation.Facade;
import com.hocs.server.saas_v2.domain.ApiInfo;
import com.hocs.server.common.ClientProjectPath;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;

@Facade
@RequiredArgsConstructor
public class ApiEndpointFacade {

	private final GitCloneService gitCloneService;
	private final ApiEndpointCollectorPort apiEndpointCollectorPort;
	private final ProjectMetaDataService projectMetaDataService;

	public ApiInfoResponse findEndpointInfo(FindApiInfoClientRequest request) {
		//IO -1
		ClientProjectPath clientProjectPath = gitCloneService.gitClone(request.getGitCloneUrl());

		//io -2
		Long metadataId = projectMetaDataService.saveProjectMetaData(
			request.getLanguage(),
			request.getProjectFramework(),
			request.getCoreSrcRootPath(),
			request.getGitCloneUrl(),
			clientProjectPath
		);

		//io -3
		//TODO pagenation 구현
		Map<ControllerFile, List<ApiInfo>> apiEndpointInfo = apiEndpointCollectorPort.findApiInfo(
			request.getLanguage(),
			request.getProjectFramework(),
			clientProjectPath,
			100);

		return new ApiInfoResponse(metadataId, apiEndpointInfo);
	}
}