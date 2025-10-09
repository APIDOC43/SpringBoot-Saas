package com.hocs.server.pipline_orchestrator.service;

import com.hocs.server.api_spec_generator.domain.input.APIMetadata;
import com.hocs.server.api_spec_generator.service.GenerateOasFacadeService;
import com.hocs.server.code_parser.core.config.GlobalJavaParser;
import com.hocs.server.code_parser.core.dataobject.JavaClassifiedStore;
import com.hocs.server.code_parser.core.domain.ClientProjectType;
import com.hocs.server.common.domain.ProjectMetaData;
import com.hocs.server.pipline_orchestrator.dto.PreProcessResult;
import com.hocs.server.pipline_orchestrator.ratelimit.PipelineTask;
import com.hocs.server.pipline_orchestrator.ratelimit.TaskContext;
import com.hocs.server.pipline_orchestrator.ratelimit.TaskContextStore;
import com.hocs.server.pipline_orchestrator.service.out.OasSendClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApiDocPipelineService {
	private final GenerateOasFacadeService llmService; //internal call로 분리
	private final OasSendClient oasSendClient;
	private final GlobalJavaParser globalJavaParser;

	public void oasGenerate(String userId, APIMetadata apiMetadata, PreProcessResult result) throws IOException {
		if (apiMetadata == null) {
			throw new RuntimeException("apiMetadata는 null일 수 없습니다.");
		}

		llmService.generate(userId, apiMetadata, result.cloneDir(), result.context().getFilenamesRelatedException(), result.requestId());
	}

	public PreProcessResult getPreProcessResult(PipelineTask task) {
		log.info("[{}] 파이프라인 실행 시작 (methodSignature={}, requestId={})", Thread.currentThread().getName(),
			task.getApiInfo().getMethodSignature(), task.getTaskId());

		String requestId = task.getTaskId();

		TaskContext context = TaskContextStore.get(task.getTaskId());
		ProjectMetaData metaData = context.getProjectMetaData();
		globalJavaParser.configure(new File(metaData.getClonePath().toFile(),
				ClientProjectType.SPRING_JAVA.srcRootPath()).toPath().toString());

		File cloneDir = metaData.getProjectRootPath().getToFile();
		PreProcessResult result = new PreProcessResult(requestId, context, metaData, cloneDir);
		return result;
	}

	public void completeProcess(PipelineTask task,  File cloneDir) {
		TaskContext taskContext = TaskContextStore.get(task.getTaskId());
		int completeCount = taskContext.incrementAndGetCompleteCount();

		log.info("[{}] 진행상황 {}/{} RequestId={}", Thread.currentThread().getName(),
			taskContext.getCompleteCount(), taskContext.getTaskSize(), task.getTaskId());
		if (completeCount == taskContext.getTaskSize()) {
			log.info("[{}] 요청에 모든 테스크가 끝났습니다. RequestId={}", Thread.currentThread().getName(),
				task.getTaskId());
			TaskContextStore.remove(task.getTaskId());
			JavaClassifiedStore.remove(task.getTaskId());
			//Task 3: OAS 데이터를 렌더링합니다.
			oasSendClient.toSaas(cloneDir, task.getTaskId());
		}
	}
}