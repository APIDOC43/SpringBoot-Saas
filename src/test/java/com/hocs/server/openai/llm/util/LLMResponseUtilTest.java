package com.hocs.server.openai.llm.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class LLMResponseUtilTest {

	private final LLMResponseUtil llmResponseUtil = new LLMResponseUtil();

	@Test
	public void testCleanYamlContent_removesBackticks() {
		String content = "```yaml\nkey: value\n```";
		String expected = "key: value";
		assertEquals(expected, llmResponseUtil.cleanYamlContent(content));
	}

	@Test
	public void testCleanYamlContent_removesDashes() {
		String content = "---\nkey: value\n---";
		String expected = "key: value";
		assertEquals(expected, llmResponseUtil.cleanYamlContent(content));
	}

	@Test
	public void testCleanYamlContent_removesYamlKeyword() {
		String content = "yaml\nkey: value";
		String expected = "key: value";
		assertEquals(expected, llmResponseUtil.cleanYamlContent(content));
	}

	@Test
	public void testCleanYamlContent_removesPipeDash() {
		String content = "|-\nkey: value";
		String expected = "key: value";
		assertEquals(expected, llmResponseUtil.cleanYamlContent(content));
	}

	@Test
	public void testCleanYamlContent_combinedCases() {
		String content = "```yaml\n---\n|-\nkey: value\n---\n```";
		String expected = "key: value";
		assertEquals(expected, llmResponseUtil.cleanYamlContent(content));
	}

	@Test
	public void testCleanYamlContent_noUnwantedCharacters() {
		String content = "key: value";
		String expected = "key: value";
		assertEquals(expected, llmResponseUtil.cleanYamlContent(content));
	}
}
