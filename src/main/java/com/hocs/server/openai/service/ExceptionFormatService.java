package com.hocs.server.openai.service;

import com.hocs.server.openai.llm.SpringAICommandForLLM;
import com.hocs.server.common.ClientProjectPath;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ExceptionFormatService {

	private final SpringAICommandForLLM springAiCommandForLLM;

	public String findRelatedExceptionSrc(ClientProjectPath projectRootPath, ChatClient chatClient4o)
		throws IOException {
		String[] ExceptionSrc = springAiCommandForLLM.findFilePathRelatedExceptionFormatSrc(
			projectRootPath, chatClient4o);

		StringBuilder sb = new StringBuilder();
		for (String src : ExceptionSrc) {
			if(!src.equals(""))
				sb.append(new String(Files.readAllBytes(Paths.get(src)))).append("\n");
		}
		return sb.toString();
	}
}
