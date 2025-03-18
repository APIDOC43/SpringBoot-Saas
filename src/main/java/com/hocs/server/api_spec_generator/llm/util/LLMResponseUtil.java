package com.hocs.server.api_spec_generator.llm.util;

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

