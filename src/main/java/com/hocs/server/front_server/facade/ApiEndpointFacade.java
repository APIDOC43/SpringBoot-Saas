package com.hocs.server.front_server.facade;

import com.hocs.server.api_doc_pipline.domain.ControllerFile;
import com.hocs.server.front_server.api.request.FindApiInfoClientRequest;
import com.hocs.server.front_server.api.response.ApiInfoResponse;
import com.hocs.server.front_server.domain.GitRepoData;
import com.hocs.server.front_server.service.GitCloneService;
import com.hocs.server.front_server.service.ProjectMetaDataService;
import com.hocs.server.front_server.service.out.ApiEndpointCollector.port.ApiEndpointCollectorPort;
import com.hocs.server.front_server.common.annotation.Facade;
import com.hocs.server.common.domain.ApiInfo;
import com.hocs.server.common.domain.ClientProjectPath;
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