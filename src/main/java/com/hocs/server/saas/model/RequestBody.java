package com.hocs.server.saas.model;

import jakarta.persistence.Access;
import java.util.Map;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "requestBody")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class RequestBody {

	private String description;
	private boolean required = false;
	private Map<String, MediaType> content;

	public static RequestBody create(String description, boolean required,
		Map<String, MediaType> content) {
		return new RequestBody(description, required, content);
	}
}