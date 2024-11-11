package com.hocs.server.openai.llm.util;

import org.springframework.stereotype.Component;

@Component
public class LLMResponseUtil {

	public String cleanYamlContent(String content) {
		content = content.replace("```", "");
		content = content.replace("---", "");
		content = content.replace("yaml", "");
		content = content.replace("|-", "");
		return content.trim();
	}
}
