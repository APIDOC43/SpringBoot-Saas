package com.hocs.server.pipline_orchestrator.facade;

import com.hocs.server.api_spec_generator.llm.SpringAICommandForLLM;
import com.hocs.server.code_parser.core.config.GlobalJavaParser;
import com.hocs.server.code_parser.core.domain.ClientProjectType;
import com.hocs.server.code_parser.facade.JavaClassifiedFacade;
import com.hocs.server.common.domain.ApiInfo;
import com.hocs.server.common.domain.DocGeneratePiplineRequest;
import com.hocs.server.common.domain.ProjectMetaData;
import com.hocs.server.pipline_orchestrator.domain.ApiInfoInPipline;
import com.hocs.server.pipline_orchestrator.domain.ControllerFile;
import com.hocs.server.pipline_orchestrator.ratelimit.PipelineTask;
import com.hocs.server.pipline_orchestrator.ratelimit.PipelineThrottleService;
import com.hocs.server.pipline_orchestrator.ratelimit.RateLimitRequestData;
import com.hocs.server.pipline_orchestrator.ratelimit.TaskClassifier;
import com.hocs.server.pipline_orchestrator.ratelimit.TaskType;
import com.hocs.server.pipline_orchestrator.ratelimit.ThrottleRequest;
import com.hocs.server.pipline_orchestrator.service.out.port.ApiEndpointCollectorPortInPipline;
import com.hocs.server.saas_platform.common.annotation.Facade;
import com.hocs.server.saas_platform.domain.GitRepoData;
import com.hocs.server.saas_platform.service.external.git.port.GitApiPort;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;

@Facade
@RequiredArgsConstructor
public class PiplineIngressFacade {

	private final SpringAICommandForLLM springAICommandForLLM;
	private final GitApiPort gitApiPort;
	private final PipelineThrottleService pipelineThrottleService;
	private final ApiEndpointCollectorPortInPipline apiEndpointCollectorPortInPipline;
	private final JavaClassifiedFacade javaClassifiedFacade;
	private final TaskClassifier taskClassifier;
	private final GlobalJavaParser globalJavaParser;


	public void ingress(DocGeneratePiplineRequest request, List<ApiInfo> excludeApiInfo)  {
		//pipline start. 파이프라인 진입점.
		//parser config 설정
		ProjectMetaData metaData = request.getProjectMetaData();
		globalJavaParser.configure(new File(metaData.getClonePath().toFile(),
				ClientProjectType.SPRING_JAVA.srcRootPath()).toPath().toString());

		ChatClient chatClient4o = springAICommandForLLM.createChatClient4o();
		String[] filenamesRelatedException = springAICommandForLLM
			.findFilePathRelatedExceptionFormatSrc(metaData.getProjectRootPath(), chatClient4o);

		GitRepoData gitRepoData = metaData.getGitRepoData();
		String defaultBranchName = gitApiPort.getDefaultBranchName(gitRepoData);

		Map<ControllerFile, List<ApiInfoInPipline>> apiEndpointInfo = apiEndpointCollectorPortInPipline.findApiInfo(
			metaData.getCodingLanguage(),
			metaData.getProjectFramework(),
			metaData.getProjectRootPath(),
			excludeApiInfo, 100);

		List<PipelineTask> tasks = apiEndpointInfo.entrySet().stream()
			.flatMap(entry -> entry.getValue().stream()
				.map(apiInfo -> new PipelineTask(entry.getKey(), apiInfo, request.getRequestId()
				)))
			.collect(Collectors.toList());

		TaskType type = taskClassifier.classify(tasks.size());
		RateLimitRequestData data = new RateLimitRequestData(request,
			excludeApiInfo, metaData, filenamesRelatedException, defaultBranchName, tasks);

		javaClassifiedFacade.initJavaClassifiedDataContainer(metaData.getClonePath(), request.getRequestId());

		pipelineThrottleService.submit(new ThrottleRequest(type,data));
	}
}