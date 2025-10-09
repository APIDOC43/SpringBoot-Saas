package com.hocs.server.pipline_orchestrator.ratelimit;


import com.hocs.server.api_spec_generator.domain.input.APIMetadata;
import com.hocs.server.common.service.GenericBatchFailureHandler;
import com.hocs.server.pipline_orchestrator.dto.ApiMetadataResult;
import com.hocs.server.pipline_orchestrator.dto.PreProcessResult;
import com.hocs.server.pipline_orchestrator.service.ApiDocPipelineService;
import com.hocs.server.pipline_orchestrator.service.UserService;
import com.hocs.server.pipline_orchestrator.service.out.port.ApiEndpointCollectorPortInPipline;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Semaphore;

@Service
@RequiredArgsConstructor
@Slf4j
public class PipelineThrottleService {

    private final PipelineTypeResolver resolver;
    private final ApiDocPipelineService pipelineService;
    private final GenericBatchFailureHandler<RestartableTaskInfo> failureHandler;
    private final ApiEndpointCollectorPortInPipline apiEndpointCollector;
    private final UserService userService;


    /**
     * 새로운 요청을 처리합니다. - 세마포어에서 바로 소비할 수 있으면 파이프라인 실행 후 작업 완료 시 release 합니다. - 토큰이 부족하면 요청을 내부 큐에 추가합니다.
     */
    public void submit(ThrottleRequest request) {
        RateLimitRequestData data = request.getData();
        List<PipelineTask> tasks = data.getTasks();
        TaskContextStore.save(request);

        String dataRequestId = request.getData().getRequest().getRequestId();
        TaskContext context = TaskContextStore.get(dataRequestId);
        String userId = context.getUserId();
        userService.addRequest(userId, dataRequestId);

        log.info("[{}] 새로운 요청이 들어왔습니다. RequestId={}", Thread.currentThread().getName(),
                request.getDataRequestId());

        for (int i = 0; i < tasks.size(); i++) {
            PipelineTask task = tasks.get(i);
            Semaphore semaphore = resolver.getRelatedSemaphore(request.getTaskType());
            if (semaphore.tryAcquire()) {
                pipelineExecuteAsync(TaskContextStore.get(request.getDataRequestId()), task, semaphore);
            } else {
                log.info("[{}] 스로틀링! 큐에 적재됩니다. RequestId={}", Thread.currentThread().getName(),
                        request.getDataRequestId());
                resolver.enqueue(task, request.getTaskType());
            }
        }
    }

    private void pipelineExecuteAsync(TaskContext context, PipelineTask task, Semaphore semaphore) {
        Executor executor = resolver.getInnerExecutor(context.getTaskType());
        String userId = context.getUserId();
        CompletableFuture
                .supplyAsync(() -> pipelineService.getPreProcessResult(task), executor)
                .thenApplyAsync(result -> getApiMetadata(task, userId, result), executor)
                .thenApplyAsync(result -> oasGenerate(userId, result), executor)
                .thenAcceptAsync(result -> pipelineService.completeProcess(task, result.cloneDir()), executor)
                .exceptionally(ex -> {
                    exceptionProcess(context, task, ex);
                    return null;
                }).whenComplete((result, ex) -> {
                    if( ex == null){
                        processQueuedRequests(task);
                    }
                    semaphore.release();
                });
    }

    private void exceptionProcess(TaskContext context, PipelineTask task, Throwable ex) {
        Throwable cause = ex instanceof CompletionException ? ex.getCause() : ex;
        log.error("[{}] Pipeline 실패 RequestId={}", Thread.currentThread().getName(), task.getTaskId(), cause);
        taskFailedProcess(context, task);
    }

    private PreProcessResult oasGenerate(String userId, ApiMetadataResult result) {
        PreProcessResult preProcessResult = result.preProcessResult();
        try {
            pipelineService.oasGenerate(userId, result.metadata(), preProcessResult);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return preProcessResult;
    }

    private ApiMetadataResult getApiMetadata(PipelineTask task, String userId, PreProcessResult result) {
        APIMetadata metadata = apiEndpointCollector.getApiEndpoint(
                userId,
                result.metaData(),
                result.context().getDefaultBranchName(),
                task,
                result.requestId()
        );
        return new ApiMetadataResult(result, metadata);
    }

    /**
     * 대기 중인 요청들을 처리합니다. - 큐에 요청이 있고, 세마포어에 토큰 소비가 가능하면 요청을 꺼내 처리합니다. - 처리 완료 후 release합니다.
     */
    public void processQueuedRequests(PipelineTask succeed) {
        TaskContext succeedContext = TaskContextStore.get(succeed.getTaskId());
        PiplineQueueService queue = resolver.getRelatedQueue(succeedContext.getTaskType());
        PipelineTask task;
        while ((task = queue.pollTask()) != null) {
            TaskContext taskContext = TaskContextStore.get(task.getTaskId());
            Semaphore semaphore = resolver.getRelatedSemaphore(taskContext.getTaskType());

            // 토큰 확보 시만 실행
            if (semaphore.tryAcquire()) {
                log.info("대기 큐 요청 처리: 토큰 소비됨");
                // pipeline 실행 완료 후 release
                pipelineExecuteAsync(taskContext, task, semaphore);
            } else {
                // 토큰 없으면 큐에 다시 넣고 종료
                resolver.enqueue(task, taskContext.getTaskType());
                break;
            }
        }
    }


    /**
     * 테스크 실패 시 대응 전략 - 재시작을 위한 정보 저장
     * - 실패 시 문서 생성을 중지하지 않고 계속 진행
     * - 재시도 없이 실패 테이블에 저장
     * - 이후 재시작할 수 있도록 필요한 모든 컨텍스트 정보 저장
     */
    void taskFailedProcess(TaskContext context, PipelineTask task) {
        try {

            // 재시작을 위한 완전한 정보 생성
            RestartableTaskInfo restartInfo = createRestartableTaskInfo(context, task);

            // 고유한 엔티티 ID 생성 (requestId + controllerFile + apiInfo)
            String entityId = generateEntityId(task);

            log.warn("파이프라인 태스크 실패 - 재시작 정보 저장: RequestId={}, EntityId={}",
                    task.getTaskId(), entityId);

            // 실패 테이블에 재시작 가능한 정보로 저장 (재시도 없음)
            failureHandler.handleFailure(restartInfo, "PIPELINE_TASK", entityId, 0);

            TaskContextStore.remove(task.getTaskId());
        } catch (Exception e) {
            // 실패 처리 자체가 실패해도 문서 생성은 계속 진행
            log.error("실패 정보 저장 중 오류 발생 - RequestId={}, Error={}",
                    task.getTaskId(), e.getMessage());
        }
    }

    /**
     * 재시작을 위한 완전한 태스크 정보 생성
     */
    private RestartableTaskInfo createRestartableTaskInfo(TaskContext context, PipelineTask task) {
        return RestartableTaskInfo.builder()
                .task(TaskInfo.builder()
                        .requestId(task.getTaskId())
                        .controllerFile(task.getControllerFile())
                        .apiInfo(task.getApiInfo())
                        .build())
                .context(ContextInfo.builder()
                        .userId(context.getUserId())
                        .defaultBranchName(context.getDefaultBranchName())
                        .filenamesRelatedException(context.getFilenamesRelatedException())
                        .projectMetaData(context.getProjectMetaData())
                        .taskType(context.getTaskType())
                        .taskSize(context.getTaskSize())
                        .build())
                .restartInfo(RestartMetaInfo.builder()
                        .canRestart(true)
                        .failureReason("EXECUTION_FAILED_RESTARTABLE")
                        .failedAt(java.time.LocalDateTime.now())
                        .build())
                .build();
    }

    /**
     * 고유한 엔티티 ID 생성
     */
    private String generateEntityId(PipelineTask task) {
        return String.format("%s_%s_%s",
                task.getTaskId(),
                task.getControllerFile().getClassName(),
                task.getApiInfo().getMethodSignature().getSignature());
    }
}