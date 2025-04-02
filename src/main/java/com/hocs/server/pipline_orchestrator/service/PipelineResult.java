package com.hocs.server.pipline_orchestrator.service;

import java.io.File;
import lombok.Data;

@Data
public class PipelineResult {

	private final File cloneDir;
	private final String userId;

	public PipelineResult(File cloneDir, String userId) {

		this.cloneDir = cloneDir;
		this.userId = userId;
	}
}