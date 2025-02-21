package com.hocs.server.front_server.service.out.ApiEndpointCollector.port;

import com.hocs.server.api_doc_pipline.domain.ControllerFile;
import com.hocs.server.common.domain.ApiInfo;
import com.hocs.server.common.domain.ClientProjectPath;
import com.hocs.server.common.domain.CodingLanguage;
import com.hocs.server.common.domain.ProjectFramework;
import java.util.List;
import java.util.Map;

public interface ApiEndpointCollectorPort {

	Map<ControllerFile, List<ApiInfo>> findApiInfo(CodingLanguage language, ProjectFramework framework,
		ClientProjectPath path, int firstPageSize);
}