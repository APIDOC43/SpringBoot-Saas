package com.hocs.server.api_spec_generator.domain.output;

import java.util.Map;
import lombok.Data;

@Data
public class PathAndComponents {
	private Map<String, PathItem> paths;
	private Components components;
}
