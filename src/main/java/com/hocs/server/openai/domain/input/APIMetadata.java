package com.hocs.server.openai.domain.input;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

// APIMetadata 클래스 정의
@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class APIMetadata {

	private final static String IMPORT_PARSE_REGEX = "(?m)^\\s*import\\s+.*?;\\s*";

	private String API;
	private String method;
	private List<String> paths;
	private List<String> globalSrc;
	private String absolutePath;

	public static APIMetadata create(String API, String method, List<String> paths, List<String> globalSrc,
		String absolutePath) {
		return new APIMetadata(API, method, paths, globalSrc, absolutePath);
	}


	public String loadSrcString() {
		// 파일 내용을 읽어 명확하게 구분된 문자열로 결합하는 메서드
		StringBuilder contents = new StringBuilder();
		for (String path : this.paths) {
			try (BufferedReader reader = new BufferedReader(
				new FileReader(path, StandardCharsets.UTF_8))) {
				String filename = Paths.get(path).getFileName().toString();
				StringBuilder fileContent = new StringBuilder();
				String line;
				while ((line = reader.readLine()) != null) {
					fileContent.append(line).append("\n");
				}

				// import 문 제거
				String leanedContent = fileContent.toString().replaceAll(IMPORT_PARSE_REGEX, "");
				// 파일 이름을 헤더로 추가하고, 코드 블록으로 소스 코드를 감쌉니다.
				contents.append("### File: ").append(filename).append("\n")
					.append("```java\n")
					.append(leanedContent)
					.append("```\n\n");
			} catch (IOException e) {
				System.err.println("Error reading file " + path + ": " + e.getMessage());
			}
		}

		return contents.toString();
	}
}
