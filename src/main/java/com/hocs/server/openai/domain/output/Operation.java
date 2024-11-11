package com.hocs.server.openai.domain.output;

import java.util.List;
import java.util.Map;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "operation")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Operation {

	private String summary;
	private String operationId;
	private String description;
	private List<String> tags;
	private List<String> x_audience; // x_audience 확장 필드
	private List<Parameter> parameters;
	private RequestBody requestBody;
	private Map<String, Response> responses;
	private Map<String, Object> extensions; // 기타 확장 필드

	public static Operation create(String summary, String operationId, String description,
		List<String> tags,
		List<String> x_audience, List<Parameter> parameters, RequestBody requestBody,
		Map<String, Response> responses, Map<String, Object> extensions) {
		return new Operation(summary, operationId, description, tags, x_audience, parameters,
			requestBody, responses, extensions);
	}
}
