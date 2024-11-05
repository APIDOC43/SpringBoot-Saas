package com.hocs.server.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.hocs.server.saas.model.OpenAPI;
import com.hocs.server.saas.model.PathItem;
import com.hocs.server.saas.model.Schema;

public class OpenAPIParser {
	public static OpenAPI parse(String oasYaml) throws JsonProcessingException {
		System.out.println(oasYaml);
		ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
		return objectMapper.readValue(oasYaml, OpenAPI.class);

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