package com.hocs.server.pipline_orchestrator.ratelimit;

import com.hocs.server.common.domain.ApiInfo;
import com.hocs.server.common.domain.DocGeneratePiplineRequest;
import com.hocs.server.common.domain.ProjectMetaData;
import java.util.List;
import lombok.Data;

@Data
public class RateLimitRequestData {

	private DocGeneratePiplineRequest request;
	private List<ApiInfo> excludeApiInfo;
	private final ProjectMetaData metaData;
	private final String[] filenamesRelatedException;
	private final String defaultBranchName;
	private final List<PipelineTask> tasks;

	public RateLimitRequestData(DocGeneratePiplineRequest request, List<ApiInfo> excludeApiInfo,
		ProjectMetaData metaData, String[] filenamesRelatedException, String defaultBranchName,
		List<PipelineTask> tasks) {
		this.request = request;
		this.excludeApiInfo = excludeApiInfo;
		this.metaData = metaData;
		this.filenamesRelatedException = filenamesRelatedException;
		this.defaultBranchName = defaultBranchName;
		this.tasks = tasks;
	}

	public int taskSize() {
		return tasks.size();
	}
}