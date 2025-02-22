package com.hocs.server.ratelimit;

import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public class RateLimitRequest {
	private final LocalDateTime arrivalTime;

	private final RateLimitRequestData data;

	public RateLimitRequest(LocalDateTime arrivalTime)
	{
		this.arrivalTime = arrivalTime;
		this.data = null;
	}

	public RateLimitRequest(LocalDateTime arrivalTime, RateLimitRequestData data) {
		this.arrivalTime = arrivalTime;
		this.data = data;
	}

	public RateLimitRequestData getData() {
		return data;
	}

	public LocalDateTime getArrivalTime() {
		return arrivalTime;
	}
}