package com.hocs.server.extractor.domain;

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

	@Field("paths")
	private List<String> paths;

	public static ExceptionHandler create(List<String> paths) {
		return new ExceptionHandler(paths);
	}
}
