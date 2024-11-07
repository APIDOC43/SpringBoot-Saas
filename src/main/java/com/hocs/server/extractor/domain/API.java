package com.hocs.server.extractor.domain;


import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

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

	private String api;
	private String method;
	private List<String> paths;

	public static API create(String api, String method, List<String> paths) {
		return new API(api, method, paths);
	}

}
