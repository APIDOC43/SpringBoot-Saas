package com.hocs.server.pipline_orchestrator.service;

import com.hocs.server.api_spec_generator.domain.input.APIMetadata;
import com.hocs.server.api_spec_generator.service.GenerateOasFacadeService;
import com.hocs.server.code_parser.core.config.GlobalJavaParser;
import com.hocs.server.code_parser.core.dataobject.JavaClassifiedStore;
import com.hocs.server.code_parser.core.domain.ClientProjectType;
import com.hocs.server.common.domain.ProjectMetaData;
import com.hocs.server.pipline_orchestrator.ratelimit.PipelineTask;
import com.hocs.server.pipline_orchestrator.ratelimit.TaskContext;
import com.hocs.server.pipline_orchestrator.ratelimit.TaskContextStore;
import com.hocs.server.pipline_orchestrator.ratelimit.TaskType;
import com.hocs.server.pipline_orchestrator.service.out.OasSendClient;
import com.hocs.server.pipline_orchestrator.service.out.port.ApiEndpointCollectorPortInPipline;
import java.io.File;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApiDocPipelineService {

	private final ApiEndpointCollectorPortInPipline apiEndpointCollectorPortInPipline;
	private final GenerateOasFacadeService llmService; //internal call로 분리
	private final OasSendClient oasSendClient;
	private final GlobalJavaParser globalJavaParser;

	public void execute(PipelineTask task) throws IOException {

		log.info("[{}] 파이프라인 실행 시작 (methodSignature={}, requestId={})", Thread.currentThread().getName(),
			task.getApiInfo().getMethodSignature(),task.getRequestId());

		String requestId = task.getRequestId();

		TaskContext context = TaskContextStore.get(task.getRequestId());
		ProjectMetaData metaData = context.getProjectMetaData();
		globalJavaParser.configure(new File(metaData.getClonePath().toFile(),
				ClientProjectType.SPRING_JAVA.srcRootPath()).toPath().toString());

		String userId = context.getUserId();

		File cloneDir = metaData.getProjectRootPath().getToFile();
		//process 1 : API 에 대한 Metadata 수집
		APIMetadata apiMetadata = apiEndpointCollectorPortInPipline.getApiEndpoint(userId, metaData,
			context.getDefaultBranchName(), task, requestId);
		log.info("process 1 done");
		//process 2 : API Endpoint 하나씩 LLM을 통해 명세를 생성합니다.
		if (apiMetadata == null) {
			throw new RuntimeException("apiMetadata는 null일 수 없습니다.");
		}

		llmService.generate(userId, apiMetadata, cloneDir,
			context.getFilenamesRelatedException(), requestId);
		log.info("process 2 done");
		//process3 : 생성된 API 명세를 정적 HTML로 렌더링
		completeProcess(task,cloneDir,userId);
		log.info("process 3 done");
	}

	public void completeProcess(PipelineTask task,  File cloneDir, String userId) {
		TaskContext taskContext = TaskContextStore.get(task.getRequestId());
		int completeCount = taskContext.incrementAndGetCompleteCount();

		log.info("[{}] 진행상황 {}/{} RequestId={}", Thread.currentThread().getName(),
			taskContext.getCompleteCount(), taskContext.getTaskSize(), task.getRequestId());
		if (completeCount == taskContext.getTaskSize()) {
			log.info("[{}] 요청에 모든 테스크가 끝났습니다. RequestId={}", Thread.currentThread().getName(),
				task.getRequestId());
			TaskContextStore.remove(task.getRequestId());
			JavaClassifiedStore.remove(task.getRequestId());
			//Task 3: OAS 데이터를 렌더링합니다.
			oasSendClient.toSaas(cloneDir, userId);
		}
	}
}