package com.hocs.server.openai.domain.output;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "oas")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
public class Schema {

	private String type;
	private String format;
	private String description;
	@JsonProperty("default")    // default 필드를 defaultValue로 매핑
	private String defaultValue;
	private Boolean nullable;
	private Map<String, Schema> properties;
	private Schema items;
	@JsonProperty("enum")    // enum 필드를 enumValue로 매핑
	private Object enumValue;
	@JsonProperty("$ref")
	private String ref; // $ref 필드
	private Object required;
	private String example;
	private Object oneOf;

	public static Schema create(String type, String format, String description, String defaultValue,
		Boolean nullable,
		Map<String, Schema> properties, Schema items, Object enumValue, String ref, Object required,
		String example, Object oneOf) {
		return new Schema(type, format, description, defaultValue, nullable, properties, items,
			enumValue, ref, required, example, oneOf);
	}
}
