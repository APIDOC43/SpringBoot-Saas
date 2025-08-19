package com.hocs.server.common.domain;

import java.util.Objects;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Data
@NoArgsConstructor
@Builder
public class ApiInfo {
	private String httpMethod; //GET, POST ...
	private String endpoint;
	private MethodInformation methodSignature; //methodName+param
	
	public ApiInfo(String httpMethod, String endpoint, MethodInformation methodSignature) {
		this.httpMethod = httpMethod;
		this.endpoint = endpoint;
		this.methodSignature = methodSignature;
	}

	// 테스트 호환성을 위한 메소드들
	public String getMethod() {
		return this.httpMethod;
	}
	
	public String getPath() {
		return this.endpoint;
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