package com.hocs.server.pipline_orchestrator.ratelimit;

import com.hocs.server.common.domain.ProjectMetaData;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ContextInfo {
    private String userId;
    private String defaultBranchName;
    private String[] filenamesRelatedException;
    private ProjectMetaData projectMetaData;
    private TaskType taskType;
    private int taskSize;
}