package com.hocs.server.pipline_orchestrator.dto;

import com.hocs.server.common.domain.ProjectMetaData;
import com.hocs.server.pipline_orchestrator.ratelimit.TaskContext;

import java.io.File;

public record PreProcessResult(String requestId, TaskContext context, ProjectMetaData metaData, File cloneDir) {
}
