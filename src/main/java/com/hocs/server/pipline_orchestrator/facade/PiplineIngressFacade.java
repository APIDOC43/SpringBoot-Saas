package com.hocs.server.pipline_orchestrator.facade;

import com.hocs.server.api_spec_generator.llm.SpringAICommandForLLM;
import com.hocs.server.common.domain.ApiInfo;
import com.hocs.server.common.domain.DocGeneratePiplineTask;
import com.hocs.server.common.domain.ProjectMetaData;
import com.hocs.server.pipline_orchestrator.service.ApiDocPipelineService;
import com.hocs.server.saas_platform.common.annotation.Facade;
import com.hocs.server.saas_platform.domain.GitRepoData;
import com.hocs.server.saas_platform.service.external.git.port.GitApiPort;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;

@Facade
@RequiredArgsConstructor
public class PiplineIngressFacade {

	private final SpringAICommandForLLM springAICommandForLLM;
	private final GitApiPort gitApiPort;
	private final ApiDocPipelineService pipelineService;

	public void ingress(DocGeneratePiplineTask request, List<ApiInfo> excludeApiInfo)  {
		//pipline start. 파이프라인 진입점.

		ProjectMetaData metaData = request.getProjectMetaData();

		ChatClient chatClient4o = springAICommandForLLM.createChatClient4o();
		String[] filenamesRelatedException = springAICommandForLLM
			.findFilePathRelatedExceptionFormatSrc(metaData.getProjectRootPath(), chatClient4o);

		GitRepoData gitRepoData = metaData.getGitRepoData();
		String defaultBranchName = gitApiPort.getDefaultBranchName(gitRepoData);

		pipelineService.executeAsync(
			request.getUserId(),
			metaData,
			filenamesRelatedException,
			defaultBranchName,
			excludeApiInfo
		);
	}

}