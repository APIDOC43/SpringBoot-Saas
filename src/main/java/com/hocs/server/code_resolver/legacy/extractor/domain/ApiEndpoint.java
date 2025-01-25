package com.hocs.server.code_resolver.legacy.extractor.domain;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Embeddable
public class ApiEndpoint {

	private String api;
	private String method;

	public static ApiEndpoint create(String api, String method) {
		return new ApiEndpoint(api, method);
	}
}
