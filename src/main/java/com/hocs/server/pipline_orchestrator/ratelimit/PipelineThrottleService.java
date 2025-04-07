package com.hocs.server.pipline_orchestrator.ratelimit;


import com.hocs.server.pipline_orchestrator.service.ApiDocPipelineService;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Semaphore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PipelineThrottleService {

	private final PipelineTypeResolver resolver;
	private final ApiDocPipelineService pipelineService;


	/**
	 * 새로운 요청을 처리합니다. - 세마포어에서 바로 소비할 수 있으면 파이프라인 실행 후 작업 완료 시 release 합니다. - 토큰이 부족하면 요청을 내부 큐에 추가합니다.
	 */
	public void submit(ThrottleRequest request) {
		RateLimitRequestData data = request.getData();
		List<PipelineTask> tasks = data.getTasks();
		TaskContextStore.save(request);

		log.info("[{}] 새로운 요청이 들어왔습니다. RequestId={}", Thread.currentThread().getName(),
			request.getDataRequestId());

		for (int i = 0; i < tasks.size(); i++) {
			PipelineTask task = tasks.get(i);
			Semaphore semaphore = resolver.getRelatedSemaphore(request.getTaskType());
			if (semaphore.tryAcquire()) {
				try {
					pipelineExecute(TaskContextStore.get(request.getDataRequestId()), task);
				}finally {
					semaphore.release();
				}
			} else {
				log.info("[{}] 스로틀링! 큐에 적재됩니다. RequestId={}", Thread.currentThread().getName(),
					request.getDataRequestId());
				resolver.enqueue(task, request.getTaskType());
			}
		}
	}

	private void pipelineExecute(TaskContext context, PipelineTask task) {
		Executor executor = resolver.getInnerExecutor(context.getTaskType());
		CompletableFuture.runAsync(() -> {
			try {
				pipelineService.execute(task);
			} catch (IOException e) {
				log.info("[{}] 테스크실패, 실패 테이블에 저장 RequestId={}", Thread.currentThread().getName(),
					task.getRequestId());
				//TODO 실패테이블 저장
				taskFailedProcess(context,task);
				throw new RuntimeException(e);
			} finally {
				log.info("[{}] 자원 해제 실행 semaphore.release()", Thread.currentThread().getName());
				processQueuedRequests(task);
			}
		},executor);
	}

	/**
	 * 대기 중인 요청들을 처리합니다. - 큐에 요청이 있고, 세마포어에 토큰 소비가 가능하면 요청을 꺼내 처리합니다. - 처리 완료 후 release합니다.
	 */
	public void processQueuedRequests(PipelineTask succeed) {
		while (true) {
			TaskContext succeedContext = TaskContextStore.get(succeed.getRequestId());
			PiplineQueueService nextQueue = resolver.getRelatedQueue(
				succeedContext.getTaskType());
			PipelineTask task = nextQueue.pollTask();
			if (task == null) {
				break;
			}
			TaskContext taskContext = TaskContextStore.get(task.getRequestId());
			Semaphore semaphore = resolver.getRelatedSemaphore(taskContext.getTaskType());

			// 큐와 토큰 버킷에 대한 연산을 원자적으로 처리
			if (semaphore.tryAcquire()) {
				try {
					log.info("대기 큐 요청 처리: 토큰 소비됨");
					pipelineExecute(taskContext, task);
				}finally {
					semaphore.release();
				}
			} else {
				break;
			}

		}
	}

	/**
	 * 테스크 실패 시 대응 전략 (미정)
	 */
	private void taskFailedProcess(TaskContext context, PipelineTask task) {
	}
}