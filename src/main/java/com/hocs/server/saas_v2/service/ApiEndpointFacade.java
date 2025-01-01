package com.hocs.server.saas_v2.service;

import com.hocs.server.saas_v2.api.request.FindApiInfoClientRequest;
import com.hocs.server.saas_v2.api.response.ApiInfoResponse;
import com.hocs.server.saas_v2.service.out.ApiEndpointCollector.port.ApiEndpointCollectorPort;
import com.hocs.server.saas_v2.common.annotation.Facade;
import com.hocs.server.saas_v2.domain.ApiInfo;
import com.hocs.server.saas_v2.domain.ClientProjectPath;
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
		ClientProjectPath path = gitCloneService.gitClone(request.getGitCloneUrl());

		//io -2
		Long metadataId = projectMetaDataService.saveProjectMetaData(
			request.getLanguage(),
			request.getProjectFramework(),
			request.getCoreSrcRootPath(),
			request.getGitCloneUrl()
		);

		//io -3
		//이 api자체가 code paser에 있어야 하는거 아닐까..? api gateway이용해서..
		////현재는 전부 주지만. enddpoint갯수는 300개이상임. 페이징하는게 좋은데, DB에 저장하고 첫 50개 보내주고 pagenagion api 만들어야 할듯
		//		//<ApiInfo:path,endpoint,className>
		Map<String, List<ApiInfo>> apiEndpointInfo = apiEndpointCollectorPort.findApiInfo(
			request.getLanguage(),
			request.getProjectFramework(),
			path,
			100);

		return new ApiInfoResponse(metadataId, apiEndpointInfo);
	}
}