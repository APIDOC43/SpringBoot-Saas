package com.hocs.server.pipline_orchestrator.ratelimit;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class RestartMetaInfo {
    private boolean canRestart;
    private String failureReason;
    private LocalDateTime failedAt;
}