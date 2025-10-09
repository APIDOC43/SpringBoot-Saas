package com.hocs.server.api_spec_generator.service;

import com.hocs.server.api_spec_generator.llm.LLMService;
import com.hocs.server.common.domain.ClientProjectPath;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExceptionFormatService {

	private final LLMService llmService;

	public String findRelatedExceptionSrc(ClientProjectPath projectRootPath, ChatClient chatClient4o)
		throws IOException {
		String[] ExceptionSrc = llmService.findFilePathRelatedExceptionFormatSrc(
			projectRootPath, chatClient4o);

		return read(ExceptionSrc);
	}

	public String read(String[] ExceptionSrc) throws IOException {
		StringBuilder sb = new StringBuilder();
		for (String src : ExceptionSrc) {
			if(!src.isEmpty()){
				log.info("src.log:{}",src);
				sb.append(new String(Files.readAllBytes(Paths.get(src)))).append("\n");
			}
		}
		return sb.toString();
	}
}
