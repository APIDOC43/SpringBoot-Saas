package com.hocs.server.openai.domain.output;


import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "oas_info")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class OasInfo {

	private String id;
	private String openapi;
	private String info;
	private String title;
	private String description;
	private String version;

	public static OasInfo create(String id, String openapi, String info, String title,
		String description,
		String version) {
		return new OasInfo(id, openapi, info, title, description, version);
	}
}
