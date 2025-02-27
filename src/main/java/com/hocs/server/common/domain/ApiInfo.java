package com.hocs.server.common.domain;

import java.util.Objects;
import lombok.Data;
import lombok.Getter;

@Getter
@Data
public class ApiInfo {
	private final String httpMethod; //GET, POST ...
	private final String endpoint;
	private final MethodInformation methodSignature; //methodName+param
	public ApiInfo(String httpMethod, String endpoint, MethodInformation methodSignature) {
		this.httpMethod = httpMethod;
		this.endpoint = endpoint;
		this.methodSignature = methodSignature;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		ApiInfo apiInfo = (ApiInfo) o;
		return Objects.equals(httpMethod, apiInfo.httpMethod) && Objects.equals(
			endpoint, apiInfo.endpoint) && Objects.equals(methodSignature,
			apiInfo.methodSignature);
	}

	@Override
	public int hashCode() {
		return Objects.hash(httpMethod, endpoint, methodSignature);
	}
}