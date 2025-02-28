package com.hocs.server.ratelimit;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.springframework.stereotype.Service;


@Service
public class RequestQueueService {

	private final Queue<RateLimitRequest> requestQueue = new ConcurrentLinkedQueue<>();

	public void addRequest(RateLimitRequest request) {
		requestQueue.add(request);
	}

	public RateLimitRequest pollRequest() {
		return requestQueue.poll();
	}

	public boolean isEmpty() {
		return requestQueue.isEmpty();
	}

	public RateLimitRequest peek() {
		return requestQueue.peek();
	}
}