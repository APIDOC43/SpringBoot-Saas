package com.hocs.server.saas_v2.domain;

import com.hocs.server.code_resolver.domain.MethodInformation;
import lombok.Getter;

@Getter
public class ApiInfo {
	private final String httpMethod; //GET, POST ...
	private final String endpoint;
	private final MethodInformation methodSignature; //methodName+param
	public ApiInfo(String httpMethod, String endpoint, MethodInformation methodSignature) {
		this.httpMethod = httpMethod;
		this.endpoint = endpoint;
		this.methodSignature = methodSignature;
	}
}