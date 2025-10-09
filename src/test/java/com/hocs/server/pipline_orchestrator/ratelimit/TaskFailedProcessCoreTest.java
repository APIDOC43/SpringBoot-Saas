package com.hocs.server.pipline_orchestrator.ratelimit;

import com.hocs.server.common.domain.MethodInformation;
import com.hocs.server.common.service.GenericBatchFailureHandler;
import com.hocs.server.pipline_orchestrator.domain.ApiInfoInPipline;
import com.hocs.server.pipline_orchestrator.domain.ControllerFile;
import com.hocs.server.pipline_orchestrator.service.UserService;
import com.hocs.server.pipline_orchestrator.service.out.port.ApiEndpointCollectorPortInPipline;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


class TaskFailedProcessCoreTest {

    @Mock
    private GenericBatchFailureHandler<RestartableTaskInfo> failureHandler;
    @Mock private UserService userService;
    @Mock private ApiEndpointCollectorPortInPipline apiEndpointCollector;

    private PipelineThrottleService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new PipelineThrottleService(null, null, failureHandler, apiEndpointCollector, userService );
    }

    @Test
    void taskFailedProcess_핵심검증_재시도없이저장() {
        TaskContext context = new TaskContext("user", "main", new String[]{}, null, TaskType.HEAVY, 1);
        PipelineTask task = createSimpleTask();

        // When
        service.taskFailedProcess(context, task);

        // Then - 핵심: 재시도 0회, PIPELINE_TASK 타입으로 저장
        verify(failureHandler, times(1)).handleFailure(
            any(RestartableTaskInfo.class),
            eq("PIPELINE_TASK"),
            anyString(),
            eq(0) // 가장 중요한 검증: 재시도 없음
        );
    }

    @Test
    void taskFailedProcess_예외발생시_중지안함() {
        // Given
        TaskContext context = new TaskContext("user", "main", new String[]{}, null, TaskType.HEAVY, 1);
        PipelineTask task = createSimpleTask();

        // 실패 핸들러가 예외 던지도록 설정
        doThrow(new RuntimeException("저장 실패")).when(failureHandler)
            .handleFailure(any(), anyString(), anyString(), anyInt());

        // When & Then - 핵심: 예외가 전파되지 않아야 함
        assertDoesNotThrow(() -> service.taskFailedProcess(context, task));
    }

    private PipelineTask createSimpleTask() {
        return new PipelineTask(
            new ControllerFile("/TestController.java"),
            ApiInfoInPipline.builder()
                .httpMethod("GET")
                .endpoint("/test")
                .methodSignature(new MethodInformation("test()"))
                .build(),
            "req-123"
        );
    }
}