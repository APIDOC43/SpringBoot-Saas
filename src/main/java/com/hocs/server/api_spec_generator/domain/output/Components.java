package com.hocs.server.api_spec_generator.domain.output;

import java.util.Map;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "components")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Components {

	private Map<String, Schema> schemas;
	public static Components create(Map<String, Schema> schemas) {
		return new Components(schemas);
	}
}
