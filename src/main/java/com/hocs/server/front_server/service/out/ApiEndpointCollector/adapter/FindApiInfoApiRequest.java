package com.hocs.server.front_server.service.out.ApiEndpointCollector.adapter;

import com.hocs.server.common.domain.ApiInfo;
import com.hocs.server.common.domain.ClientProjectPath;
import com.hocs.server.common.domain.CodingLanguage;
import com.hocs.server.common.domain.ProjectFramework;
import java.util.List;
import lombok.Data;

@Data
public class FindApiInfoApiRequest {

	private final CodingLanguage language;
	private final ProjectFramework projectFramework;
	private final ClientProjectPath path;
	private final int firstPageSize;
	private final List<ApiInfo> excludeFile;

	public FindApiInfoApiRequest(CodingLanguage language, ProjectFramework projectFramework,
		ClientProjectPath path, int firstPageSize, List<ApiInfo> excludeFile) {
		this.language = language;
		this.projectFramework = projectFramework;
		this.path = path;
		this.firstPageSize = firstPageSize;
		this.excludeFile = excludeFile;
	}
}