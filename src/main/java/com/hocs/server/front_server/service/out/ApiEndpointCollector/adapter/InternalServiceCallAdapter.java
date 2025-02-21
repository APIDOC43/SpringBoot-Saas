package com.hocs.server.front_server.service.out.ApiEndpointCollector.adapter;

import com.hocs.server.custom_rag.facade.ApiEndpointResolveFacade;
import com.hocs.server.api_doc_pipline.domain.ControllerFile;
import com.hocs.server.front_server.common.annotation.Adapter;
import com.hocs.server.common.domain.ApiInfo;
import com.hocs.server.common.domain.ClientProjectPath;
import com.hocs.server.common.domain.CodingLanguage;
import com.hocs.server.common.domain.ProjectFramework;
import com.hocs.server.front_server.service.out.ApiEndpointCollector.port.ApiEndpointCollectorPort;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;


@Adapter
@RequiredArgsConstructor
public class InternalServiceCallAdapter implements ApiEndpointCollectorPort {

	private final ApiEndpointResolveFacade service;

	@Override
	public Map<ControllerFile, List<ApiInfo>> findApiInfo(CodingLanguage language, ProjectFramework projectFramework,
		ClientProjectPath path, int firstPageSize) {

		FindApiInfoApiRequest request = new FindApiInfoApiRequest(language,
			projectFramework, path, firstPageSize, new ArrayList<>());

		return service.findApiInfo(request);
	}
}