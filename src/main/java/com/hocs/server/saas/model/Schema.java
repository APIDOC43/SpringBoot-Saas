package com.hocs.server.saas.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import lombok.Data;

@Data
public class Schema {
	private String type;
	private String format;
	private String description;
	@JsonProperty("default")	// default 필드를 defaultValue로 매핑
	private String defaultValue;
	private Boolean nullable;
	private Map<String, Schema> properties;
	private Schema items;
	@JsonProperty("enum")	// enum 필드를 enumValue로 매핑
	private Object enumValue;
	@JsonProperty("$ref")
	private String ref; // $ref 필드
	private Object required;
	private String example;
	private Object oneOf;

}
