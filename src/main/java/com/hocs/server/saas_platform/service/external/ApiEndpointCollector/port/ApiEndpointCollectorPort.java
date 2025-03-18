package com.hocs.server.saas_platform.service.external.ApiEndpointCollector.port;

import com.hocs.server.common.domain.ApiInfo;
import com.hocs.server.common.domain.ClientProjectPath;
import com.hocs.server.common.domain.CodingLanguage;
import com.hocs.server.common.domain.ProjectFramework;
import com.hocs.server.pipline_orchestrator.domain.ControllerFile;
import java.util.List;
import java.util.Map;

public interface ApiEndpointCollectorPort {

	Map<ControllerFile, List<ApiInfo>> findApiInfo(CodingLanguage language, ProjectFramework framework,
		ClientProjectPath path, int firstPageSize);
}