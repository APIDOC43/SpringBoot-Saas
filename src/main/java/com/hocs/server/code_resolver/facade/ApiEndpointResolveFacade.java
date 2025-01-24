package com.hocs.server.code_resolver.facade;

import com.hocs.server.code_resolver.domain.APIEntries;
import com.hocs.server.code_resolver.domain.ControllerFile;
import com.hocs.server.code_resolver.service.ApiEndpointCollectorService;
import com.hocs.server.saas_v2.common.annotation.Facade;
import com.hocs.server.saas_v2.domain.ApiInfo;
import com.hocs.server.saas_v2.service.out.ApiEndpointCollector.adapter.FindApiInfoApiRequest;
import java.io.File;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;


@Facade
@RequiredArgsConstructor
public class ApiEndpointResolveFacade {

	private final com.hocs.server.code_resolver.service.ApiInfoExtractorService ApiInfoExtractorService;
	private final ApiEndpointCollectorService apiEndpointCollectorService;

	public Map<ControllerFile, List<ApiInfo>> findApiInfo(FindApiInfoApiRequest request) {
		APIEntries apiEntries = apiEndpointCollectorService.findControllerFiles(
			request.getLanguage(), request.getProjectFramework(), request.getPath());

		List<File> controllerFiles = apiEntries.getFiles();

		return ApiInfoExtractorService.extractApiInfo(controllerFiles);
	}
}