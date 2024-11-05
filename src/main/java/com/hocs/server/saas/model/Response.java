package com.hocs.server.saas.model;

import java.util.Map;
import lombok.Data;

@Data
public class Response {
	private String description;
	private Map<String, MediaType> content;
//	private Headers headers;
	private Object headers;
	// getters and setters
}
