package com.hocs.server.api_spec_generator.domain.output;

import com.hocs.server.saas_platform.domain.MongoBaseEntity;
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
public class OAS extends MongoBaseEntity {

	@Id
	private String id;
	private String snippetId;

	private OasInfo info;
	private Map<String, List<Map<String, PathItem>>> pathList;
	private Map<String, List<Schema>> schemasMap;

	public static OAS create(String id, String snippetId, OasInfo info,
		Map<String, List<Map<String, PathItem>>> pathList,
		Map<String, List<Schema>> schemasMap) {
		return new OAS(id,snippetId, info, pathList, schemasMap);
	}
}
