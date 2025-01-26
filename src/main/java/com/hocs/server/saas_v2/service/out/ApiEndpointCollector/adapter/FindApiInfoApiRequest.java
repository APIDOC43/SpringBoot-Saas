package com.hocs.server.saas_v2.service.out.ApiEndpointCollector.adapter;

import com.hocs.server.common.domain.ClientProjectPath;
import com.hocs.server.common.domain.CodingLanguage;
import com.hocs.server.common.domain.ProjectFramework;
import lombok.Data;

@Data
public class FindApiInfoApiRequest {

	private final CodingLanguage language;
	private final ProjectFramework projectFramework;
	private final ClientProjectPath path;
	private final int firstPageSize;

	public FindApiInfoApiRequest(CodingLanguage language, ProjectFramework projectFramework,
		ClientProjectPath path, int firstPageSize) {
		this.language = language;
		this.projectFramework = projectFramework;
		this.path = path;
		this.firstPageSize = firstPageSize;
	}
}