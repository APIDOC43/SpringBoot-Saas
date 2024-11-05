package com.hocs.server.saas.model;

import java.util.Map;
import lombok.Data;

@Data
public class OpenAPI {
	private Map<String, PathItem> paths;
	private Components components;
}
