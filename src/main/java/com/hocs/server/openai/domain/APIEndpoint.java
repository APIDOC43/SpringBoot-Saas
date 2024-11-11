package com.hocs.server.openai.domain;

import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

// APIEndpoint 클래스 정의
@Getter
@Setter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class APIEndpoint {

	private String API;
	private String method;
	private List<String> paths;
	private String src;
	private String globalSrc;

	private String absolutePath;



	// 매개변수가 있는 생성자
	public APIEndpoint(String API, String method, List<String> paths, String absolutePath) {
		this.API = API;
		this.method = method;
		this.paths = paths;
		this.src = "";
		this.absolutePath = absolutePath;
	}
}
