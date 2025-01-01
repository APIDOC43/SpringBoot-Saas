package com.hocs.server.saas_v2.domain;

import lombok.Getter;

@Getter
public class ApiInfo {
	private final String method;
	private final String endpoint;

	public ApiInfo(String method, String endpoint) {
		this.method = method;
		this.endpoint = endpoint;
	}
}