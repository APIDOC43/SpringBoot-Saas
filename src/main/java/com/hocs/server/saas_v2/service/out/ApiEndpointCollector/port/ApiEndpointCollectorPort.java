package com.hocs.server.saas_v2.service.out.ApiEndpointCollector.port;

import com.hocs.server.api_doc_pipline.domain.ControllerFile;
import com.hocs.server.saas_v2.domain.ApiInfo;
import com.hocs.server.common.ClientProjectPath;
import com.hocs.server.common.CodingLanguage;
import com.hocs.server.common.ProjectFramework;
import java.util.List;
import java.util.Map;

public interface ApiEndpointCollectorPort {

	Map<ControllerFile, List<ApiInfo>> findApiInfo(CodingLanguage language, ProjectFramework framework,
		ClientProjectPath path, int firstPageSize);
}