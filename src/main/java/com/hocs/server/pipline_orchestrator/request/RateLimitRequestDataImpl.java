package com.hocs.server.pipline_orchestrator.request;

import com.hocs.server.common.domain.ApiInfo;
import com.hocs.server.common.domain.DocGeneratePiplineTask;
import com.hocs.server.common.domain.ProjectMetaData;
import com.hocs.server.pipline_orchestrator.ratelimit.RateLimitRequestData;
import java.util.List;
import lombok.Data;

@Data
public class RateLimitRequestDataImpl implements RateLimitRequestData {
	private DocGeneratePiplineTask request;
	private List<ApiInfo> excludeApiInfo;
	private final ProjectMetaData metaData;
	private final String[] filenamesRelatedException;
	private final String defaultBranchName;

	public RateLimitRequestDataImpl(DocGeneratePiplineTask request, List<ApiInfo> excludeApiInfo,
		ProjectMetaData metaData, String[] filenamesRelatedException, String defaultBranchName) {
		this.request = request;
		this.excludeApiInfo = excludeApiInfo;
		this.metaData = metaData;
		this.filenamesRelatedException = filenamesRelatedException;
		this.defaultBranchName = defaultBranchName;
	}
}