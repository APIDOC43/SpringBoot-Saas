package com.hocs.server.saas.model;

import jakarta.persistence.Access;
import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "parameter")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Parameter {

	private String name;
	private String in; // query, header, path, cookie 등
	private String description;
	private boolean required = false;
	private Object example;
	private Schema schema;

	public static Parameter create(String name, String in, String description, boolean required,
		Object example,
		Schema schema) {
		return new Parameter(name, in, description, required, example, schema);
	}
}
