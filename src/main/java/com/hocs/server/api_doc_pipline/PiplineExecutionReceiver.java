package com.hocs.server.api_doc_pipline;

import com.hocs.server.api_doc_pipline.service.ApiDocPiplineService;
import com.hocs.server.openai.llm.SpringAICommandForLLM;
import com.hocs.server.common.ProjectMetaData;
import com.hocs.server.saas_v2.domain.ApiInfo;
import com.hocs.server.saas_v2.domain.UrlData;
import com.hocs.server.saas_v2.service.out.git.port.GitApiPort;
import com.hocs.server.saas_v2.service.out.pipline.adapter.GenerationRequest;
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

	public void send(GenerationRequest request)  {
		//pipline start. 파이프라인 진입점.

		ProjectMetaData metaData = request.getMetaData();

		ChatClient chatClient4o = springAICommandForLLM.createChatClient4o();
		String[] filenamesRelatedException = springAICommandForLLM
			.findFilePathRelatedExceptionFormatSrc(metaData.getProjectRootPath(), chatClient4o);

		String gitCloneUrl = metaData.getGitCloneUrl();
		String defaultBranchName = gitApiPort.getDefaultBranchName(UrlData.of(gitCloneUrl));

		try {
			List<ApiInfo> excludeApiInfo = request.getExcludeApiInfo();
			apiDocPiplineService.execute(
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