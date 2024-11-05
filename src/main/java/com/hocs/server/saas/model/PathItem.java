package com.hocs.server.saas.model;

import java.util.Map;
import lombok.Data;

@Data
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

}
