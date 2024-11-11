package com.hocs.server.openai.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.hocs.server.openai.domain.output.PathAndComponents;
import com.hocs.server.openai.domain.output.PathItem;
import com.hocs.server.openai.domain.output.Schema;

public class OpenAPIParser {
	public static PathAndComponents parse(String oasYaml) throws JsonProcessingException {
		System.out.println(oasYaml);
		ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
		return objectMapper.readValue(oasYaml, PathAndComponents.class);

	}

	public static Schema parseToSchema(String oasYaml) throws JsonProcessingException {
		System.out.println(oasYaml);
		ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
		return objectMapper.readValue(oasYaml, Schema.class);

	}

	public static PathItem parseToPath(String integrationPath) {
		System.out.println(integrationPath);
		ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
		try {
			return objectMapper.readValue(integrationPath, PathItem.class);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}
}