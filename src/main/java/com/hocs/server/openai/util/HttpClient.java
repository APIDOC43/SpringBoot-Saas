package com.hocs.server.openai.util;

import java.io.File;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Slf4j
public class HttpClient {
	private final static String BASE_URL = "http://localhost:8080";
	public static String findHtmlRequest(String filename) {
		RestTemplate restTemplate = new RestTemplate();
		String url = BASE_URL+"/demo/content";

		// JSON 요청 본문 생성
		String jsonBody = String.format("{\"filename\": \"%s\"}", filename);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		HttpEntity<String> requestEntity = new HttpEntity<>(jsonBody, headers);

		try {
			ResponseEntity<String> response = restTemplate.postForEntity(url, requestEntity, String.class);
			return response.getBody();
		} catch (Exception e) {
			System.err.println("Error while making API request: " + e.getMessage());
			return null;
		}
	}

	/** sass server에 전송**/
	public static void toSaas(File projectDir, String userId) {
		// 업로드할 파일 경로
		File file = new File(projectDir.getAbsolutePath() + "/output_file-fix.yaml");

		// 파일 존재 여부 확인
		if (!file.exists()) {
			System.err.println("파일이 존재하지 않습니다: ");
			return;
		}

		if (!file.isFile()) {
			System.err.println("지정된 경로가 파일이 아닙니다: ");
			return;
		}
		FileSystemResource fileResource = new FileSystemResource(file);

		// MultiValueMap 생성
		MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
		body.add("file", fileResource);

		// 헤더 설정
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.MULTIPART_FORM_DATA);

		// HttpEntity 생성
		HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

		// RestTemplate 인스턴스 생성
		RestTemplate restTemplate = new RestTemplate();

		// 요청 URL

		String serverUrl = BASE_URL + "/demo/api/v1/oas?userId=" + userId;

		// POST 요청 보내기
		ResponseEntity<String> response = restTemplate.postForEntity(serverUrl, requestEntity,
			String.class);

		// 응답 출력
		log.info("Response: {}",response.getBody());
	}

	public static void main(String[] args) {
		toSaas(new File("/Users/hong/Desktop/hong/soma/alone/practice/saas-server/hocsserver"), "hong");
	}

}
