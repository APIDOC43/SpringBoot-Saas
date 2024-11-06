package com.hocs.server.extractor.domain;

import jakarta.persistence.Access;
import jakarta.persistence.Id;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "exception_handler")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ExceptionHandler {

	@Id
	String id;

	@Field("paths")
	private List<String> paths;

	public static ExceptionHandler create(String id, List<String> paths) {
		return new ExceptionHandler(id, paths);
	}
}
