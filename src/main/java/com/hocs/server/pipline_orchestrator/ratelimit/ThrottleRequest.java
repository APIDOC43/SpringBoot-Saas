package com.hocs.server.pipline_orchestrator.ratelimit;

import lombok.Data;

@Data
public class ThrottleRequest {
	private final TaskType taskType;
	private final RateLimitRequestData data;

	public ThrottleRequest(TaskType taskType, RateLimitRequestData data) {
		this.taskType = taskType;
		this.data = data;
	}


	public String getDataRequestId() {
		return data.getRequest().getRequestId();
	}
}