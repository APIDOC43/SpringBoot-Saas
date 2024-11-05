package com.hocs.server.saas.model;
import java.util.Map;
import lombok.Data;

@Data
public class RequestBody {
	private String description;
	private boolean required = false;
	private Map<String, MediaType> content;
}