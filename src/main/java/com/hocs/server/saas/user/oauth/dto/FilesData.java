package com.hocs.server.saas.user.oauth.dto;

import lombok.Data;

@Data
public
class FilesData {

	String filename;
	String filePath;

	public FilesData(String filePath) {
		this.filePath = filePath;
		this.filename = parseEndpoint(filePath);
	}

	public String parseEndpoint(String filePath) {
		// 마지막 슬래시를 기준으로 파일명 추출
		String fileName = filePath.substring(filePath.lastIndexOf('/') + 1);

		// 확장자 제거
		fileName = fileName.replace(".html", "");

		// 언더스코어(_)로 구분된 부분 분리
		String[] parts = fileName.split("__", 2);

		if (parts.length != 2) {
			throw new IllegalArgumentException("Invalid file name format.");
		}

		// HTTP 메서드 부분 추출
		String httpMethod = parts[0];

		// 엔드포인트 부분 변환
		String endpoint = parts[1].replace('_', '/');
		endpoint = endpoint.replaceAll("\\{([^}]+)\\}", "{$1}"); // {param} 형식 유지

		return "[" + httpMethod + "] " + endpoint;
	}
}
