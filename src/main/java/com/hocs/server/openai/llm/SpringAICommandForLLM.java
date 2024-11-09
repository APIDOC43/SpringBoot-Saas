package com.hocs.server.openai.llm;


import static org.springframework.ai.openai.api.OpenAiApi.ChatModel.GPT_4_O;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.hocs.server.extractor.core.SrcFileCollector;
import com.hocs.server.extractor.domain.ClientProjectType;
import com.hocs.server.saas.model.OpenAPI;
import com.hocs.server.saas.model.PathItem;
import com.hocs.server.saas.model.Schema;
import com.hocs.server.openai.domain.APIEntry;
import com.hocs.server.openai.llm.exception.LLMException;
import com.hocs.server.openai.util.FileManager;
import com.hocs.server.util.OpenAPIParser;
import com.hocs.server.util.cli.CLIManager;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
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
@Slf4j
public class SpringAICommandForLLM {

	@Value("${openai.api-key}")
	private String apiKey;

	public String integrationOas(StringBuffer totalContent, ChatClient client) {
		log.info("----------------------------");
		ChatClientPromptRequestSpec requestPath = client.prompt(
			new Prompt(PromptMessageHub.createOasBasedSnippet(totalContent.toString())));
		String content = getResultContent(requestPath.call());
		content = cleanYamlContent(content);
		return content;
	}



	public OpenAPI requestOasApiSnippet(ChatClient client, APIEntry apiEntry, int time,String srcRelationErrorForamt)
		throws JsonProcessingException {
		threadSleep(time);
		log.info("-------------------------------");

		String promptStr = PromptMessageHub.createOasPathSection(apiEntry,srcRelationErrorForamt);
		ChatClientPromptRequestSpec requestPath = client.prompt(new Prompt(promptStr));
		String pathContent = getResultContent(requestPath.call());

		//valid
		String validPrompt = PromptMessageHub.createOasDescriptionDetail(apiEntry,pathContent);
		ChatClientPromptRequestSpec validRequest = client.prompt(new Prompt(validPrompt));
		String result = getResultContent(validRequest.call());

		//errorResponse format valid
		String formatValidPrompt = PromptMessageHub.vaildErrorResponseFormat(apiEntry,srcRelationErrorForamt,result);
		ChatClientPromptRequestSpec formatValidRequest = client.prompt(new Prompt(formatValidPrompt));
		result = getResultContent(formatValidRequest.call());

		String str = cleanYamlContent(result);
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

	private String cleanYamlContent(String content) {
		content = content.replace("```", "");
		content = content.replace("---", "");
		content = content.replace("yaml", "");
		content = content.replace("|-", "");
		return content.trim();
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
		return cleanYamlContent(content);

	}

	public String integrationPath(List<Map<String, PathItem>> maps, ChatClient client) {
		ChatClientPromptRequestSpec requestPath = client.prompt(
			new Prompt(PromptMessageHub.integrationPath(maps)));
		String content = getResultContent(requestPath.call());
		return cleanYamlContent(content);

	}

	public String complementOas(OpenAPI result,List<APIEntry> apiEntries, SrcFileCollector srcFileCollector,
		ChatClient client,String ProjectRoot) {

		List<File> notUesedSrc = srcFileCollector.getNotUesedSrc(apiEntries,ProjectRoot,
			ClientProjectType.SPRING_JAVA);

		String src = FileManager.loadFileContents(
			notUesedSrc.stream()
				.map(File::getAbsolutePath)
				.collect(Collectors.toList())
		);

		src = src+apiEntries.get(0).getGlobalSrc();

		ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
		objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
		try {
			String yamlString = objectMapper.writeValueAsString(result);

			String promptStr = PromptMessageHub.complementAPI(yamlString, src);

			ChatClientPromptRequestSpec requestPath = client.prompt(
				new Prompt(promptStr));
			String content = getResultContent(requestPath.call());
			return cleanYamlContent(content);

		}catch (RuntimeException e){
			if(e.getMessage().equals("TPM"))
				throw new RuntimeException("TPM");
			throw new RuntimeException(e);
		}
		catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
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
