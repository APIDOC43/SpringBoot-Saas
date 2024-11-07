package com.hocs.server.saas.model;

import java.util.Map;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "oas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PathItem {

	private Operation get;
	private Operation post;
	private Operation put;
	private Operation delete;
	private Operation options;
	private Operation head;
	private Operation patch;
	private Operation trace;
	private String x_link;
	private Map<String, Object> extensions; // x_ 로 시작하는 확장 필드

	public static PathItem create(Operation get, Operation post, Operation put, Operation delete,
		Operation options,
		Operation head, Operation patch, Operation trace, String x_link,
		Map<String, Object> extensions) {
		return new PathItem(get, post, put, delete, options, head, patch, trace, x_link,
			extensions);
	}

}
