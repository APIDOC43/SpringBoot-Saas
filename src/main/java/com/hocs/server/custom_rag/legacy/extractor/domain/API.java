package com.hocs.server.custom_rag.legacy.extractor.domain;


import jakarta.persistence.Embedded;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "api")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class API {
	/* [example]
	API: /product/search/
	method: POST
	paths: []
 	**/

	@Embedded
	private ApiEndpoint apiEndpoint;
	private List<String> paths = new ArrayList<>();

	@Field("link")
	private String link;
	public static API create(ApiEndpoint apiEndpoint, List<String> paths) {
		return new API(apiEndpoint, paths, "empty");
	}


	public void setLink(String link) {
		this.link = link;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		API api = (API) o;
		return Objects.equals(apiEndpoint, api.apiEndpoint);
	}

	@Override
	public int hashCode() {
		return Objects.hash(apiEndpoint, paths, link);
	}
}
