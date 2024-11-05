package com.hocs.server.saas.model;

import lombok.Data;

@Data
public class Parameter {
	private String name;
	private String in; // query, header, path, cookie 등
	private String description;
	private boolean required = false;
	private Object example;
	private Schema schema;

}
