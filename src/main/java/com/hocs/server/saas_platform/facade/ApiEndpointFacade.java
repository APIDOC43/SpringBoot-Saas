package com.hocs.server.saas_platform.facade;

import com.hocs.server.common.domain.ApiInfo;
import com.hocs.server.common.domain.ClientProjectPath;
import com.hocs.server.pipline_orchestrator.domain.ControllerFile;
import com.hocs.server.saas_platform.common.annotation.Facade;
import com.hocs.server.saas_platform.controller.request.FindApiInfoClientRequest;
import com.hocs.server.saas_platform.controller.response.ApiInfoResponse;
import com.hocs.server.saas_platform.domain.GitRepoData;
import com.hocs.server.saas_platform.service.GitCloneService;
import com.hocs.server.saas_platform.service.ProjectMetaDataService;
import com.hocs.server.saas_platform.service.external.ApiEndpointCollector.port.ApiEndpointCollectorPort;
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
			GitRepoData.of(request.getGitCloneUrl()),
			clientProjectPath
		);

		//io -3
		//TODO pagenation 구현
		Map<ControllerFile, List<ApiInfo>> apiEndpointInfo = apiEndpointCollectorPort.findApiInfo(
			request.getLanguage(),
			request.getProjectFramework(),
			clientProjectPath,
			100);

		int apiCount = apiEndpointInfo.values().stream().mapToInt(List::size).sum();
		return new ApiInfoResponse(apiEndpointInfo.size(), apiCount,metadataId, apiEndpointInfo);
	}
}