package com.hocs.server.extractor.domain;

import jakarta.persistence.Id;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "api_source_dependency_info")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class APISourceDependencyInfo {

	@Id
	private String id;
	private String userId;

	@Field("apis")
	private List<API> apiSourceDependencies;

	@Field("global")
	private GlobalSourceDependency global;

	public static APISourceDependencyInfo create(String id, String userId,
		List<API> apiSourceDependencies,
		GlobalSourceDependency global) {
		return new APISourceDependencyInfo(id, userId, apiSourceDependencies, global);
	}
}
