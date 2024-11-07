package com.hocs.server.saas.model;

import jakarta.persistence.Id;
import java.util.List;
import java.util.Map;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "oas")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class OAS {

	@Id
	private String id;

	private OasInfo info;
	private Map<String, List<Map<String, PathItem>>> pathList;
	private Map<String, List<Schema>> schemasMap;

	public static OAS create(String id, OasInfo info,
		Map<String, List<Map<String, PathItem>>> pathList,
		Map<String, List<Schema>> schemasMap) {
		return new OAS(id, info, pathList, schemasMap);
	}
}
