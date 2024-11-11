package com.hocs.server.openai.domain.output;

import java.util.Map;
import lombok.Data;

@Data
public class OpenAPI {
	private Map<String, PathItem> paths;
	private Components components;
}
