package com.hocs.server.api_spec_generator.llm;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.hocs.server.api_spec_generator.domain.input.APIMetadata;
import com.hocs.server.api_spec_generator.domain.output.PathAndComponents;
import com.hocs.server.api_spec_generator.domain.output.Schema;
import com.hocs.server.common.domain.ClientProjectPath;
import java.util.List;
import org.springframework.ai.chat.client.ChatClient;

public interface LLMService {

	PathAndComponents requestOasApiSnippet(ChatClient client, APIMetadata apiMetadata, int time, String srcRelationErrorFormat)
		throws JsonProcessingException;

	String integrationSchema(List<Schema> schemas, ChatClient client);

	ChatClient createChatClient4o();

	String[] findFilePathRelatedExceptionFormatSrc(ClientProjectPath projectPath, ChatClient client);
}