package com.hocs.server.pipline_orchestrator.facade;

import com.hocs.server.api_spec_generator.llm.SpringAICommandForLLM;
import com.hocs.server.code_parser.core.config.GlobalJavaParser;
import com.hocs.server.code_parser.facade.JavaClassifiedFacade;
import com.hocs.server.common.domain.*;
import com.hocs.server.pipline_orchestrator.domain.ApiInfoInPipline;
import com.hocs.server.pipline_orchestrator.domain.ControllerFile;
import com.hocs.server.pipline_orchestrator.ratelimit.PipelineThrottleService;
import com.hocs.server.pipline_orchestrator.ratelimit.TaskClassifier;
import com.hocs.server.pipline_orchestrator.ratelimit.TaskType;
import com.hocs.server.pipline_orchestrator.ratelimit.ThrottleRequest;
import com.hocs.server.pipline_orchestrator.service.out.port.ApiEndpointCollectorPortInPipline;
import com.hocs.server.saas_platform.domain.GitRepoData;
import com.hocs.server.saas_platform.service.external.git.port.GitApiPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PiplineIngressFacadeTest {

    @Mock
    private GlobalJavaParser globalJavaParser;

    @Mock
    private SpringAICommandForLLM springAICommandForLLM;

    @Mock
    private GitApiPort gitApiPort;

    @Mock
    private PipelineThrottleService pipelineThrottleService;

    @Mock
    private ApiEndpointCollectorPortInPipline apiEndpointCollectorPortInPipline;

    @Mock
    private JavaClassifiedFacade javaClassifiedFacade;

    @Mock
    private TaskClassifier taskClassifier;

    @Mock
    private ChatClient chatClient;

    @InjectMocks
    private PiplineIngressFacade piplineIngressFacade;

    private DocGeneratePiplineRequest request;
    private ProjectMetaData metaData;
    private GitRepoData gitRepoData;
    private List<ApiInfo> excludeApiInfo;
    @BeforeEach
    void setUp() {
        gitRepoData = GitRepoData.builder()
                .url("https://github.com/test/repo")
                .token("test-token")
                .build();

        metaData = ProjectMetaData.builder()
                .projectRootPath(ClientProjectPath.of("/test/project"))
                .srcRootPath("/test/clone")
                .codingLanguage(CodingLanguage.JAVA)
                .projectFramework(ProjectFramework.SPRING_BOOT)
                .gitRepoData(gitRepoData)
                .build();

        request = DocGeneratePiplineRequest.builder()
                .requestId("test-request-id")
                .projectMetaData(metaData)
                .build();

        excludeApiInfo = Arrays.asList(
                ApiInfo.builder().endpoint("/api/test1").httpMethod("GET").build(),
                ApiInfo.builder().endpoint("/api/test2").httpMethod("POST").build()
        );
    }

    @Test
    @DisplayName("파이프라인 진입점에서 정상적으로 모든 단계를 수행한다")
    void shouldIngressPipelineSuccessfully() {
        // Given
        String[] exceptionFiles = {"Exception1.java", "Exception2.java"};
        String defaultBranch = "main";
        
        ControllerFile controllerFile = ControllerFile.builder()
                .filePath("/test/controller/UserController.java")
                .build();
        
        ApiInfoInPipline apiInfoInPipline = ApiInfoInPipline.builder()
                .httpMethod("GET")
                .endpoint("/api/users")
                .build();
        
        Map<ControllerFile, List<ApiInfoInPipline>> apiEndpointInfo = new HashMap<>();
        apiEndpointInfo.put(controllerFile, Arrays.asList(apiInfoInPipline));

        when(springAICommandForLLM.createChatClient4o()).thenReturn(chatClient);
        when(springAICommandForLLM.findFilePathRelatedExceptionFormatSrc(
                eq(metaData.getProjectRootPath()), eq(chatClient)))
                .thenReturn(exceptionFiles);
        when(gitApiPort.getDefaultBranchName(gitRepoData)).thenReturn(defaultBranch);
        when(apiEndpointCollectorPortInPipline.findApiInfo(
                eq(CodingLanguage.JAVA), eq(ProjectFramework.SPRING_BOOT), eq(metaData.getProjectRootPath()), eq(excludeApiInfo), eq(100)))
                .thenReturn(apiEndpointInfo);
        when(taskClassifier.classify(1)).thenReturn(TaskType.LIGHT);

        // When
        piplineIngressFacade.ingress(request, excludeApiInfo);

        // Then
        verify(springAICommandForLLM).createChatClient4o();
        verify(springAICommandForLLM).findFilePathRelatedExceptionFormatSrc(
                eq(metaData.getProjectRootPath()), eq(chatClient));
        verify(gitApiPort).getDefaultBranchName(gitRepoData);
        verify(apiEndpointCollectorPortInPipline).findApiInfo(
                eq(CodingLanguage.JAVA), eq(ProjectFramework.SPRING_BOOT), eq(metaData.getProjectRootPath()), eq(excludeApiInfo), eq(100));
        verify(taskClassifier).classify(1);
        verify(javaClassifiedFacade).initJavaClassifiedDataContainer(
                eq(metaData.getClonePath()), eq("test-request-id"));
        verify(pipelineThrottleService).submit(any(ThrottleRequest.class));
    }

    @Test
    @DisplayName("API 엔드포인트가 여러 개일 때 올바른 태스크 개수로 분류된다")
    void shouldClassifyTasksCorrectlyWithMultipleEndpoints() {
        // Given
        String[] exceptionFiles = {"Exception.java"};
        String defaultBranch = "main";
        
        ControllerFile controllerFile1 = ControllerFile.builder()
                .filePath("/test/controller/UserController.java")
                .build();
        ControllerFile controllerFile2 = ControllerFile.builder()
                .filePath("/test/controller/OrderController.java")
                .build();
        
        ApiInfoInPipline userApi = ApiInfoInPipline.builder()
                .httpMethod("GET")
                .endpoint("/api/users")
                .build();
        ApiInfoInPipline orderApi1 = ApiInfoInPipline.builder()
                .httpMethod("GET")
                .endpoint("/api/orders")
                .build();
        ApiInfoInPipline orderApi2 = ApiInfoInPipline.builder()
                .httpMethod("POST")
                .endpoint("/api/orders")
                .build();
        
        Map<ControllerFile, List<ApiInfoInPipline>> apiEndpointInfo = new HashMap<>();
        apiEndpointInfo.put(controllerFile1, Arrays.asList(userApi));
        apiEndpointInfo.put(controllerFile2, Arrays.asList(orderApi1, orderApi2));

        when(springAICommandForLLM.createChatClient4o()).thenReturn(chatClient);
        when(springAICommandForLLM.findFilePathRelatedExceptionFormatSrc(any(ClientProjectPath.class), any()))
                .thenReturn(exceptionFiles);
        when(gitApiPort.getDefaultBranchName(any())).thenReturn(defaultBranch);
        when(apiEndpointCollectorPortInPipline.findApiInfo(any(CodingLanguage.class), any(ProjectFramework.class), any(ClientProjectPath.class), any(), anyInt()))
                .thenReturn(apiEndpointInfo);
        when(taskClassifier.classify(3)).thenReturn(TaskType.MEDIUM);

        // When
        piplineIngressFacade.ingress(request, excludeApiInfo);

        // Then
        verify(taskClassifier).classify(3); // 총 3개의 API 엔드포인트
    }

    @Test
    @DisplayName("빈 API 엔드포인트 정보로도 정상 처리된다")
    void shouldHandleEmptyApiEndpoints() {
        // Given
        String[] exceptionFiles = {};
        String defaultBranch = "develop";
        Map<ControllerFile, List<ApiInfoInPipline>> emptyApiEndpointInfo = new HashMap<>();

        when(springAICommandForLLM.createChatClient4o()).thenReturn(chatClient);
        when(springAICommandForLLM.findFilePathRelatedExceptionFormatSrc(any(ClientProjectPath.class), any()))
                .thenReturn(exceptionFiles);
        when(gitApiPort.getDefaultBranchName(any())).thenReturn(defaultBranch);
        when(apiEndpointCollectorPortInPipline.findApiInfo(any(CodingLanguage.class), any(ProjectFramework.class), any(ClientProjectPath.class), any(), anyInt()))
                .thenReturn(emptyApiEndpointInfo);
        when(taskClassifier.classify(0)).thenReturn(TaskType.LIGHT);

        // When
        piplineIngressFacade.ingress(request, excludeApiInfo);

        // Then
        verify(taskClassifier).classify(0);
        verify(pipelineThrottleService).submit(any(ThrottleRequest.class));
    }

    @Test
    @DisplayName("null 제외 API 목록으로도 정상 처리된다")
    void shouldHandleNullExcludeApiInfo() {
        // Given
        String[] exceptionFiles = {"Exception.java"};
        String defaultBranch = "main";
        Map<ControllerFile, List<ApiInfoInPipline>> apiEndpointInfo = new HashMap<>();

        when(springAICommandForLLM.createChatClient4o()).thenReturn(chatClient);
        when(springAICommandForLLM.findFilePathRelatedExceptionFormatSrc(any(ClientProjectPath.class), any()))
                .thenReturn(exceptionFiles);
        when(gitApiPort.getDefaultBranchName(any())).thenReturn(defaultBranch);
        when(apiEndpointCollectorPortInPipline.findApiInfo(any(CodingLanguage.class), any(ProjectFramework.class), any(ClientProjectPath.class), eq(null), anyInt()))
                .thenReturn(apiEndpointInfo);
        when(taskClassifier.classify(0)).thenReturn(TaskType.LIGHT);

        // When
        piplineIngressFacade.ingress(request, null);

        // Then
        verify(apiEndpointCollectorPortInPipline).findApiInfo(
                any(CodingLanguage.class), any(ProjectFramework.class), any(ClientProjectPath.class), eq(null), anyInt());
    }
}
