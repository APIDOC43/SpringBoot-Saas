package com.hocs.server.api_spec_generator.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.hocs.server.api_spec_generator.domain.output.PathAndComponents;
import com.hocs.server.api_spec_generator.domain.output.PathItem;
import com.hocs.server.api_spec_generator.domain.output.Schema;

public class OpenAPIParser {
	public static PathAndComponents parse(String oasYaml) throws JsonProcessingException {
		ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
		return objectMapper.readValue(oasYaml, PathAndComponents.class);

	}

	public static Schema parseToSchema(String oasYaml) throws JsonProcessingException {
		ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
		return objectMapper.readValue(oasYaml, Schema.class);

	}

	public static PathItem parseToPath(String integrationPath) {
		ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
		try {
			return objectMapper.readValue(integrationPath, PathItem.class);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}
}