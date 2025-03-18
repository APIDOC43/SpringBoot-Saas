package com.hocs.server.pipline_orchestrator.throttling;

import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public class PipelineRequest {
	private final LocalDateTime arrivalTime;

	private final PipelineRequestData data;

	public PipelineRequest(LocalDateTime arrivalTime)
	{
		this.arrivalTime = arrivalTime;
		this.data = null;
	}

	public PipelineRequest(LocalDateTime arrivalTime, PipelineRequestData data) {
		this.arrivalTime = arrivalTime;
		this.data = data;
	}

	public PipelineRequestData getData() {
		return data;
	}

	public LocalDateTime getArrivalTime() {
		return arrivalTime;
	}
}