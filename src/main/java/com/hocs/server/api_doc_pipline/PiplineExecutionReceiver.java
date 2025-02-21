package com.hocs.server.api_doc_pipline;

import com.hocs.server.api_doc_pipline.service.ApiDocPiplineService;
import com.hocs.server.openai.llm.SpringAICommandForLLM;
import com.hocs.server.common.domain.ProjectMetaData;
import com.hocs.server.common.domain.ApiInfo;
import com.hocs.server.common.domain.DocGeneratePiplineTask;
import com.hocs.server.front_server.domain.GitRepoData;
import com.hocs.server.front_server.service.out.git.port.GitApiPort;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PiplineExecutionReceiver {

	private final SpringAICommandForLLM springAICommandForLLM;
	private final ApiDocPiplineService apiDocPiplineService;
	private final GitApiPort gitApiPort;

	public void receive(DocGeneratePiplineTask request, List<ApiInfo> excludeApiInfo)  {
		//pipline start. 파이프라인 진입점.

		ProjectMetaData metaData = request.getProjectMetaData();

		ChatClient chatClient4o = springAICommandForLLM.createChatClient4o();
		String[] filenamesRelatedException = springAICommandForLLM
			.findFilePathRelatedExceptionFormatSrc(metaData.getProjectRootPath(), chatClient4o);

		GitRepoData gitRepoData = metaData.getGitRepoData();
		String defaultBranchName = gitApiPort.getDefaultBranchName(gitRepoData);

		try {
			apiDocPiplineService.executeAsync(
				request.getUserId(),
				metaData,
				filenamesRelatedException,
				defaultBranchName,
				excludeApiInfo
			);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

}