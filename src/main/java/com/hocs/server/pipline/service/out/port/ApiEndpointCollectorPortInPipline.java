package com.hocs.server.pipline.service.out.port;

import com.hocs.server.pipline.domain.ApiInfoInPipline;
import com.hocs.server.pipline.domain.ControllerFile;
import com.hocs.server.common.domain.ApiInfo;
import com.hocs.server.common.domain.CodingLanguage;
import com.hocs.server.common.domain.ProjectFramework;
import com.hocs.server.common.domain.ClientProjectPath;
import com.hocs.server.common.domain.ProjectMetaData;
import com.hocs.server.openai.domain.input.APIMetadata;
import java.util.List;
import java.util.Map;

public interface ApiEndpointCollectorPortInPipline {

	Map<ControllerFile, List<ApiInfoInPipline>> findApiInfo(
		CodingLanguage language,
		ProjectFramework framework,
		ClientProjectPath path,
		List<ApiInfo> excludeApiInfo,
		int firstPageSize);


	List<APIMetadata> getApiEndpoints(
		String userId,
		ProjectMetaData metaData,
		String defaultBranchName,
		ControllerFile controllerFile,
		String requestId);

}