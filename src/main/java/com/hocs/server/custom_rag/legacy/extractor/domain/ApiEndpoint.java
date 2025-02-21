package com.hocs.server.custom_rag.legacy.extractor.domain;

import jakarta.persistence.Embeddable;
import java.util.Objects;
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

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		ApiEndpoint that = (ApiEndpoint) o;
		return Objects.equals(api, that.api) && Objects.equals(method, that.method);
	}

	@Override
	public int hashCode() {
		return Objects.hash(api, method);
	}
}
