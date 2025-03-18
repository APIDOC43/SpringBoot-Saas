package com.hocs.server.pipline_orchestrator.throttling;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.springframework.stereotype.Service;


@Service
public class RequestQueueService {

	private final Queue<PipelineRequest> requestQueue = new ConcurrentLinkedQueue<>();

	public void addRequest(PipelineRequest request) {
		requestQueue.add(request);
	}

	public PipelineRequest pollRequest() {
		return requestQueue.poll();
	}

	public boolean isEmpty() {
		return requestQueue.isEmpty();
	}

	public PipelineRequest peek() {
		return requestQueue.peek();
	}
}