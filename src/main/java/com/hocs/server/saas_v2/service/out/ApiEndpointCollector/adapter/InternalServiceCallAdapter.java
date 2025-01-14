package com.hocs.server.saas_v2.service.out.ApiEndpointCollector.adapter;

import com.hocs.server.code_resolver.collector.service.ApiEndpointResolveFacade;
import com.hocs.server.saas_v2.common.annotation.Adapter;
import com.hocs.server.saas_v2.domain.ApiInfo;
import com.hocs.server.saas_v2.domain.ClientProjectPath;
import com.hocs.server.saas_v2.domain.CodingLanguage;
import com.hocs.server.saas_v2.domain.ProjectFramework;
import com.hocs.server.saas_v2.service.out.ApiEndpointCollector.port.ApiEndpointCollectorPort;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;


@Adapter
@RequiredArgsConstructor
public class InternalServiceCallAdapter implements ApiEndpointCollectorPort {

	private final ApiEndpointResolveFacade service;

	@Override
	public Map<String, List<ApiInfo>> findApiInfo(CodingLanguage language, ProjectFramework projectFramework,
		ClientProjectPath path, int firstPageSize) {

		FindApiInfoApiRequest request = new FindApiInfoApiRequest(language,
			projectFramework, path, firstPageSize);

		return service.findApiInfo(request);
	}
}