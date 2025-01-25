package com.hocs.server.api_doc_pipline.service.out.port;

import com.hocs.server.api_doc_pipline.domain.ApiInfoInPipline;
import com.hocs.server.api_doc_pipline.domain.ControllerFile;
import com.hocs.server.common.CodingLanguage;
import com.hocs.server.common.ProjectFramework;
import com.hocs.server.common.ClientProjectPath;
import com.hocs.server.common.ProjectMetaData;
import com.hocs.server.openai.domain.input.APIMetadata;
import java.util.List;
import java.util.Map;

public interface ApiEndpointCollectorPortInPipline {

	Map<ControllerFile, List<ApiInfoInPipline>> findApiInfo(CodingLanguage language, ProjectFramework framework,
		ClientProjectPath path, int firstPageSize);


	List<APIMetadata> getApiEndpoints(String userId, ProjectMetaData metaData, String defaultBranchName, ControllerFile controllerFile);

}