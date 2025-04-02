package com.hocs.server.pipline_orchestrator.service.out.port;

import com.hocs.server.api_spec_generator.domain.input.APIMetadata;
import com.hocs.server.common.domain.ApiInfo;
import com.hocs.server.common.domain.ClientProjectPath;
import com.hocs.server.common.domain.CodingLanguage;
import com.hocs.server.common.domain.ProjectFramework;
import com.hocs.server.common.domain.ProjectMetaData;
import com.hocs.server.pipline_orchestrator.domain.ApiInfoInPipline;
import com.hocs.server.pipline_orchestrator.domain.ControllerFile;
import com.hocs.server.pipline_orchestrator.ratelimit.PipelineTask;
import java.util.List;
import java.util.Map;

public interface ApiEndpointCollectorPortInPipline {

	Map<ControllerFile, List<ApiInfoInPipline>> findApiInfo(
		CodingLanguage language,
		ProjectFramework framework,
		ClientProjectPath path,
		List<ApiInfo> excludeApiInfo,
		int firstPageSize);


	APIMetadata getApiEndpoint(
		String userId,
		ProjectMetaData metaData,
		String defaultBranchName,
		PipelineTask task,
		String requestId);

}