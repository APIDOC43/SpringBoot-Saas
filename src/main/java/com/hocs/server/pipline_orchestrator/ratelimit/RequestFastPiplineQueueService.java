package com.hocs.server.pipline_orchestrator.ratelimit;

import jakarta.annotation.PostConstruct;
import java.util.concurrent.ArrayBlockingQueue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class RequestFastPiplineQueueService implements PiplineQueueService{

	@Value("${pipeline.queue.heavy.size}")
	private int QUEUE_SIZE;

	private ArrayBlockingQueue<PipelineTask> requestQueue;
	@PostConstruct
	public void initQueue() {
		this.requestQueue = new ArrayBlockingQueue<>(QUEUE_SIZE);
	}
	public void addTask(PipelineTask request) {
		requestQueue.add(request);
	}

	@Override
	public int size() {
		return requestQueue.size();
	}

	@Override
	public int maxSize() {
		return QUEUE_SIZE;
	}


	public PipelineTask pollTask() {
		return requestQueue.poll();
	}

	public boolean isEmpty() {
		return requestQueue.isEmpty();
	}

	public PipelineTask peek() {
		return requestQueue.peek();
	}
}