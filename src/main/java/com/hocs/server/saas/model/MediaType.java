package com.hocs.server.saas.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "mediaType")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MediaType {

	private Schema schema;
	private String description;

	public static MediaType create(Schema schema, String description) {
		return new MediaType(schema, description);
	}
}