package com.hocs.server.openai.llm;


import static org.springframework.ai.openai.api.OpenAiApi.ChatModel.GPT_4_O;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.hocs.server.openai.domain.APIEndpoint;
import com.hocs.server.openai.llm.exception.LLMException;
import com.hocs.server.openai.llm.util.LLMResponseUtil;
import com.hocs.server.saas.model.OpenAPI;
import com.hocs.server.saas.model.Schema;
import com.hocs.server.util.OpenAPIParser;
import com.hocs.server.util.cli.CLIManager;
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

	public OpenAPI requestOasApiSnippet(ChatClient client, APIEndpoint apiEndpoint, int time,String srcRelationErrorFormat)
		throws JsonProcessingException {
		threadSleep(time);
		log.info("-------------------------------");

		//createOasPathSection
		String promptStr = PromptMessageHub.createOasPathSection(apiEndpoint,srcRelationErrorFormat);
		ChatClientPromptRequestSpec requestPath = client.prompt(new Prompt(promptStr));
		String pathContent = getResultContent(requestPath.call());

		//createOasDescriptionDetail
		String validPrompt = PromptMessageHub.createOasDescriptionDetail(apiEndpoint,pathContent);
		ChatClientPromptRequestSpec validRequest = client.prompt(new Prompt(validPrompt));
		String result = getResultContent(validRequest.call());

		//validErrorResponseFormat
		String formatValidPrompt = PromptMessageHub.validErrorResponseFormat(srcRelationErrorFormat,result);
		ChatClientPromptRequestSpec formatValidRequest = client.prompt(new Prompt(formatValidPrompt));
		result = getResultContent(formatValidRequest.call());

		String str = llmResponseUtil.cleanYamlContent(result);
		OpenAPI parse = OpenAPIParser.parse(str);

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
		String content = getResultContent(requestPath.call());
		return llmResponseUtil.cleanYamlContent(content);

	}

	public ChatClient createChatClient4o() {
		OpenAiApi openAiApi = new OpenAiApi(
			apiKey); // 실제 API 키로 교체하세요
		ChatModel chatModel = new OpenAiChatModel(openAiApi,
			OpenAiChatOptions.builder().withModel(GPT_4_O).build());

		ChatClient client = ChatClient.builder(chatModel)
			.build();
		return client;
	}

	public String[] findFilePathRelatedExceptionFormatSrc(String TEMP_DIR,ChatClient client) {
		CLIManager cliManager = new CLIManager();
		String output = cliManager.executeCommand(new String[]{"tree", TEMP_DIR + "/src/main/java"});

		System.out.println("tree output = " + output);


		String validPrompt = PromptMessageHub.findRelationExceptionFormat(output);
		ChatClientPromptRequestSpec validRequest = client.prompt(new Prompt(validPrompt));
		String result = getResultContent(validRequest.call());

		System.out.println("result = " + result);
		String[] split = result.split(",");


		return split;
	}
}
