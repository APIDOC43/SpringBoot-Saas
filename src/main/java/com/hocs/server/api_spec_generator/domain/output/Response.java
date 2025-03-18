package com.hocs.server.api_spec_generator.domain.output;

import java.util.Map;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "response")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Response {

	private String description;
	private Map<String, MediaType> content;
	private Object headers;

	public static Response create(String description, Map<String, MediaType> content,
		Object headers) {
		return new Response(description, content, headers);
	}
}
