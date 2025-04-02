package com.hocs.server.pipline_orchestrator.ratelimit;

public interface PiplineQueueService {

	boolean isEmpty();
	PipelineTask pollTask();
	void addTask(PipelineTask request);
	int size();
	int maxSize();
}