package com.hocs.server.api_spec_generator.llm;


import static org.springframework.ai.openai.api.OpenAiApi.ChatModel.GPT_4_O;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.hocs.server.api_spec_generator.domain.input.APIMetadata;
import com.hocs.server.api_spec_generator.domain.output.PathAndComponents;
import com.hocs.server.api_spec_generator.domain.output.Schema;
import com.hocs.server.api_spec_generator.llm.exception.LLMException;
import com.hocs.server.api_spec_generator.llm.util.LLMResponseUtil;
import com.hocs.server.api_spec_generator.util.OpenAPIParser;
import com.hocs.server.common.domain.ClientProjectPath;
import com.hocs.server.saas_platform.service.CLIManager;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClient.CallPromptResponseSpec;
import org.springframework.ai.chat.client.ChatClient.ChatClientPromptRequestSpec;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SpringAICommandForLLM {

	@Value("${openai.api-key}")
	private String apiKey;

	private final LLMResponseUtil llmResponseUtil;

	public PathAndComponents requestOasApiSnippet(ChatClient client, APIMetadata apiMetadata, int time,String srcRelationErrorFormat)
		throws JsonProcessingException {
		threadSleep(time);
		log.info("[{}]-------------------------------",Thread.currentThread().getName());

		//createOasPathSection
		//TODO Fake 버전, 인자로 변경할 수 있게 수정
		String promptStr = PromptMessageHub.createOasPathSection(apiMetadata,srcRelationErrorFormat);
		ChatClientPromptRequestSpec requestPath = client.prompt(new Prompt(promptStr));
		String pathContent = getResultContent(requestPath.call());
		// String pathContent = FakeResponse.pathContent();


		//createOasDescriptionDetail
		String validPrompt = PromptMessageHub.createOasDescriptionDetail(apiMetadata,pathContent);
		ChatClientPromptRequestSpec validRequest = client.prompt(new Prompt(validPrompt));
		String result = getResultContent(validRequest.call());
		// String result = FakeResponse.createDescrionion();

		//validErrorResponseFormat
		String formatValidPrompt = PromptMessageHub.validErrorResponseFormat(srcRelationErrorFormat,result);
		ChatClientPromptRequestSpec formatValidRequest = client.prompt(new Prompt(formatValidPrompt));
		result = getResultContent(formatValidRequest.call());
		// result = FakeResponse.fomatValid();

		String str = llmResponseUtil.cleanYamlContent(result);
		PathAndComponents parse = OpenAPIParser.parse(str);
		log.info("[{}]end-------------------------------",Thread.currentThread().getName());
		return parse;
	}

	private String getResultContent(CallPromptResponseSpec response) {
		try {

			ChatResponse chatResponse = response.chatResponse();
			Generation result = chatResponse.getResult();
			AssistantMessage output = result.getOutput();
		return output.getContent();
		}catch (RuntimeException e){
			throw new LLMException("TPM");
		}

	}



	private void threadSleep(int sleep) {
		try {
			Thread.sleep(sleep);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}


	public String integrationSchema(List<Schema> schemas, ChatClient client) {
		String contents = PromptMessageHub.integrationSchema(schemas);
		ChatClientPromptRequestSpec requestPath = client.prompt(
			new Prompt(contents));
//		String content = FakeResponse.fomatValid();
		String content = getResultContent(requestPath.call());
		return llmResponseUtil.cleanYamlContent(content);

	}

	public ChatClient createChatClient4o() {
		OpenAiApi openAiApi = new OpenAiApi(
			apiKey);
		ChatModel chatModel = new OpenAiChatModel(openAiApi,
			OpenAiChatOptions.builder().withModel(GPT_4_O).build());

		ChatClient client = ChatClient.builder(chatModel)
			.build();
		return client;
	}

	public String[] findFilePathRelatedExceptionFormatSrc(ClientProjectPath projectPath,ChatClient client) {
		CLIManager cliManager = new CLIManager();
		String output = cliManager.executeCommand(new String[]{"tree", projectPath.getPath().toString() + "/src/main/java"});

		String validPrompt = PromptMessageHub.findRelationExceptionFormat(output);
		ChatClientPromptRequestSpec validRequest = client.prompt(new Prompt(validPrompt));
		String result = getResultContent(validRequest.call());
		String[] split = result.split(",");
		return split;
	}
}
