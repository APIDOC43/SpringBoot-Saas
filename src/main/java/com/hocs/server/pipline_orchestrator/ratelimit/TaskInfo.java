package com.hocs.server.pipline_orchestrator.ratelimit;

import com.hocs.server.pipline_orchestrator.domain.ApiInfoInPipline;
import com.hocs.server.pipline_orchestrator.domain.ControllerFile;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TaskInfo {
    private String requestId;
    private ControllerFile controllerFile;
    private ApiInfoInPipline apiInfo;
}