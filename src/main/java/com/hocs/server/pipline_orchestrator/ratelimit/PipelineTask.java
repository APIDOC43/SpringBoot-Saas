package com.hocs.server.pipline_orchestrator.ratelimit;

import com.hocs.server.pipline_orchestrator.domain.ApiInfoInPipline;
import com.hocs.server.pipline_orchestrator.domain.ControllerFile;
import lombok.Data;

@Data
public class PipelineTask {
	private final ControllerFile controllerFile;
	private final ApiInfoInPipline apiInfo;
	private final String requestId;

	public PipelineTask(ControllerFile controllerFile, ApiInfoInPipline apiInfo, String requestId) {
		this.controllerFile = controllerFile;
		this.apiInfo = apiInfo;
		this.requestId = requestId;
	}
}