package com.hocs.server.api_doc_pipline.domain;

import com.hocs.server.common.domain.MethodInformation;
import lombok.Getter;

@Getter
public class ApiInfoInPipline {
	private final String httpMethod; //GET, POST ...
	private final String endpoint;
	private final MethodInformation methodSignature; //methodName+param
	public ApiInfoInPipline(String httpMethod, String endpoint, MethodInformation methodSignature) {
		this.httpMethod = httpMethod;
		this.endpoint = endpoint;
		this.methodSignature = methodSignature;
	}
}