package com.hocs.server.pipline_orchestrator.ratelimit;

import java.util.concurrent.Executor;
import java.util.concurrent.Semaphore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class PipelineTypeResolver {
	@Autowired
	@Qualifier("HeavyAsyncExecutor")
	private Executor heavyAsyncExecutor;
	@Autowired
	@Qualifier("HeavyQueueSemaphore")
	private Semaphore heavyQueueSemaphore;
	@Autowired
	@Qualifier("FastAsyncExecutor")
	private Executor fastAsyncExecutor;
	@Autowired
	@Qualifier("FastQueueSemaphore")
	private Semaphore fastQueueSemaphore;

	private final RequestHeavyPiplineQueueService heavyQueueService;
	private final RequestFastPiplineQueueService fastQueueService;

	public Executor getInnerExecutor(TaskType type) {
		return (type == TaskType.FAST) ? fastAsyncExecutor : heavyAsyncExecutor;
	}

	public PiplineQueueService getRelatedQueue(TaskType type) {
		return (type == TaskType.FAST) ? fastQueueService : heavyQueueService;
	}

	public void enqueue(PipelineTask task, TaskType taskType) {
		PiplineQueueService relatedQueue = getRelatedQueue(taskType);
		relatedQueue.addTask(task);
		log.info("[{}] {} queue 상태 {}/{}",Thread.currentThread().getName(),taskType,relatedQueue.size(),relatedQueue.maxSize());
	}

	public Semaphore getRelatedSemaphore(TaskType type) {
		return (type == TaskType.FAST) ? fastQueueSemaphore : heavyQueueSemaphore;
	}
}