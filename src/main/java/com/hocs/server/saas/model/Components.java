package com.hocs.server.saas.model;
import java.util.Map;
import lombok.Data;

@Data
public class Components {
	private Map<String, Schema> schemas;
}
