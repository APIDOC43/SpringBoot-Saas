package com.hocs.server.pipline_orchestrator.ratelimit;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RestartableTaskInfo {
    private TaskInfo task;
    private ContextInfo context;
    private RestartMetaInfo restartInfo;
}