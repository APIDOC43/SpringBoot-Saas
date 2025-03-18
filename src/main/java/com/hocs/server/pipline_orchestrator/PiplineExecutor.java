package com.hocs.server.pipline_orchestrator;

import com.hocs.server.api_spec_generator.llm.SpringAICommandForLLM;
import com.hocs.server.common.domain.ApiInfo;
import com.hocs.server.common.domain.DocGeneratePiplineTask;
import com.hocs.server.common.domain.ProjectMetaData;
import com.hocs.server.pipline_orchestrator.ratelimit.Bucket4jRateLimitRequestService;
import com.hocs.server.pipline_orchestrator.ratelimit.RateLimitRequest;
import com.hocs.server.pipline_orchestrator.request.RateLimitRequestDataImpl;
import com.hocs.server.saas_platform.domain.GitRepoData;
import com.hocs.server.saas_platform.service.external.git.port.GitApiPort;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PiplineExecutor {

	private final SpringAICommandForLLM springAICommandForLLM;
	private final GitApiPort gitApiPort;
	private final Bucket4jRateLimitRequestService rateLimitRequestService;

	public void receive(DocGeneratePiplineTask request, List<ApiInfo> excludeApiInfo)  {
		//pipline start. 파이프라인 진입점.

		ProjectMetaData metaData = request.getProjectMetaData();

		ChatClient chatClient4o = springAICommandForLLM.createChatClient4o();
		String[] filenamesRelatedException = springAICommandForLLM
			.findFilePathRelatedExceptionFormatSrc(metaData.getProjectRootPath(), chatClient4o);

		GitRepoData gitRepoData = metaData.getGitRepoData();
		String defaultBranchName = gitApiPort.getDefaultBranchName(gitRepoData);

		RateLimitRequest rateLimitRequest = RateLimitRequest
			.builder()
			.arrivalTime(LocalDateTime.now())
			.data(new RateLimitRequestDataImpl(request,excludeApiInfo,metaData,filenamesRelatedException,defaultBranchName))
			.build();

		rateLimitRequestService.handleNewRequest(rateLimitRequest);
	}

}