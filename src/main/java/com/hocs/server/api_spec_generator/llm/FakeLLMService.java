package com.hocs.server.api_spec_generator.llm;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.hocs.server.api_spec_generator.domain.input.APIMetadata;
import com.hocs.server.api_spec_generator.domain.output.PathAndComponents;
import com.hocs.server.api_spec_generator.domain.output.Schema;
import com.hocs.server.api_spec_generator.llm.util.LLMResponseUtil;
import com.hocs.server.api_spec_generator.util.OpenAPIParser;
import com.hocs.server.common.domain.ClientProjectPath;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
@Primary
public class FakeLLMService implements LLMService {

	private final LLMResponseUtil llmResponseUtil;

	@Override
	public PathAndComponents requestOasApiSnippet(ChatClient client, APIMetadata apiMetadata, int time, String srcRelationErrorFormat)
		throws JsonProcessingException {
		threadSleep(time);
		log.info("[{}] FAKE LLM Service - requestOasApiSnippet", Thread.currentThread().getName());

		String pathContent = FakeResponse.pathContent();
		String result = FakeResponse.createDescrionion();
		result = FakeResponse.fomatValid();

        try {
            Thread.sleep(30000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        String str = llmResponseUtil.cleanYamlContent(result);
		PathAndComponents parse = OpenAPIParser.parse(str);
		log.info("[{}] FAKE LLM Service - requestOasApiSnippet completed", Thread.currentThread().getName());
		return parse;
	}

	@Override
	public String integrationSchema(List<Schema> schemas, ChatClient client) {
		log.info("FAKE LLM Service - integrationSchema");
		String content = FakeResponse.fomatValid();
		return llmResponseUtil.cleanYamlContent(content);
	}

	@Override
	public ChatClient createChatClient4o() {
		log.info("FAKE LLM Service - createChatClient4o (returning null for fake)");
		return null;
	}

	@Override
	public String[] findFilePathRelatedExceptionFormatSrc(ClientProjectPath projectPath, ChatClient client) {
		return new String[0];
	}

	private void threadSleep(int sleep) {
		try {
			Thread.sleep(sleep);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}
}