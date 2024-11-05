package com.hocs.server.saas.model;
import java.util.List;
import java.util.Map;
import lombok.Data;

@Data
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
}
