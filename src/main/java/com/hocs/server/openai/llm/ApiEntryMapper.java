//package org.example.plugin.openai;
//
//import java.io.BufferedReader;
//import java.io.FileInputStream;
//import java.io.FileReader;
//import java.io.FileWriter;
//import java.io.IOException;
//import java.nio.charset.StandardCharsets;
//import java.nio.file.Paths;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//import org.yaml.snakeyaml.Yaml;
//
//// APIEntry 클래스 정의
//public class ApiEntryMapper {
//
//	public static class APIEntry {
//		private String API;
//		private String method;
//		private List<String> paths;
//		private String src;
//
//		// 기본 생성자
//		public APIEntry() {}
//
//		// 매개변수가 있는 생성자
//		public APIEntry(String API, String method, List<String> paths, String src) {
//			this.API = API;
//			this.method = method;
//			this.paths = paths;
//			this.src = src;
//		}
//
//		// Getter 및 Setter
//		public String getAPI() {
//			return API;
//		}
//
//		public void setAPI(String API) {
//			this.API = API;
//		}
//
//		public String getMethod() {
//			return method;
//		}
//
//		public void setMethod(String method) {
//			this.method = method;
//		}
//
//		public List<String> getPaths() {
//			return paths;
//		}
//
//		public void setPaths(List<String> paths) {
//			this.paths = paths;
//		}
//
//		public String getSrc() {
//			return src;
//		}
//
//		public void setSrc(String src) {
//			this.src = src;
//		}
//
//		@Override
//		public String toString() {
//			return "APIEntry{" +
//				"API='" + API + '\'' +
//				", method='" + method + '\'' +
//				", paths=" + paths +
//				", src='" + src + '\'' +
//				'}';
//		}
//	}
//
//	// 파일 내용을 읽어 명확하게 구분된 문자열로 결합하는 메서드
//	public static String loadFileContents(List<String> filePaths) {
//		StringBuilder contents = new StringBuilder();
//		for (String path : filePaths) {
//			try (BufferedReader reader = new BufferedReader(new FileReader(path, StandardCharsets.UTF_8))) {
//				String filename = Paths.get(path).getFileName().toString();
//				StringBuilder fileContent = new StringBuilder();
//				String line;
//				while ((line = reader.readLine()) != null) {
//					fileContent.append(line).append("\n");
//				}
//				// import 문 제거
//				String leanedContent = fileContent.toString().replaceAll("(?m)^\\s*import\\s+.*?;\\s*$", "");
//				// 파일 이름을 헤더로 추가하고, 코드 블록으로 소스 코드를 감쌉니다.
//				contents.append("### File: ").append(filename).append("\n")
//					.append("```java\n")
//					.append(leanedContent)
//					.append("```\n\n");
//			} catch (IOException e) {
//				System.out.println("Error reading file " + path + ": " + e.getMessage());
//			}
//		}
//		return contents.toString();
//	}
//
//	// 단일 파일 내용을 읽어 명확하게 구분된 문자열로 결합하는 메서드
//	public static String loadSingleFileContents(String filePath) {
//		StringBuilder contents = new StringBuilder();
//		try (BufferedReader reader = new BufferedReader(new FileReader(filePath, StandardCharsets.UTF_8))) {
//			String filename = Paths.get(filePath).getFileName().toString();
//			StringBuilder fileContent = new StringBuilder();
//			String line;
//			while ((line = reader.readLine()) != null) {
//				fileContent.append(line).append("\n");
//			}
//			// 파일 이름을 헤더로 추가하고, 코드 블록으로 소스 코드를 감쌉니다.
//			contents.append("### File: ").append(filename).append("\n")
//				.append("```java\n")
//				.append(fileContent.toString())
//				.append("```\n\n");
//		} catch (IOException e) {
//			System.out.println("Error reading file " + filePath + ": " + e.getMessage());
//		}
//		return contents.toString();
//	}
//
//	// YAML 파일을 읽어 APIEntry 리스트로 변환하는 메서드
//	public static List<APIEntry> loadApiYaml(String filePath) {
//		List<APIEntry> apiEntries = new ArrayList<>();
//		Yaml yaml = new Yaml();
//		try (FileInputStream fis = new FileInputStream(filePath)) {
//			Object data = yaml.load(fis);
//			if (data instanceof List) {
//				List<?> yamlData = (List<?>) data;
//				for (Object item : yamlData) {
//					if (item instanceof Map) {
//						Map<?, ?> entryMap = (Map<?, ?>) item;
//						if (entryMap.containsKey("API")) {
//							String API = entryMap.get("API").toString();
//							String method = entryMap.get("method").toString();
//							List<String> paths = new ArrayList<>();
//							Object pathsObj = entryMap.get("paths");
//							if (pathsObj instanceof List) {
//								for (Object path : (List<?>) pathsObj) {
//									paths.add(path.toString());
//								}
//							}
//							APIEntry apiEntry = new APIEntry(API, method, paths, "");
//							apiEntries.add(apiEntry);
//						}
//					}
//				}
//			}
//		} catch (IOException e) {
//			System.out.println("Error reading YAML file " + filePath + ": " + e.getMessage());
//		} catch (Exception e) {
//			System.out.println("Error parsing YAML file: " + e.getMessage());
//		}
//		return apiEntries;
//	}
//
//	// OpenAPI 내용을 전처리하는 메서드 (부분적으로 구현됨)
//	public static String preprocessOpenapiContent(String con) {
//		// 전처리 로직을 구현해야 합니다.
//		// 현재는 입력된 문자열을 그대로 반환합니다.
//		return con;
//	}
//
//	// 전처리된 OpenAPI 내용을 YAML 파일로 저장하는 메서드
//	public static void saveOpenapiYaml(String preprocessedCon, String outputFile) {
//		try (FileWriter writer = new FileWriter(outputFile, StandardCharsets.UTF_8)) {
//			writer.write(preprocessedCon);
//			System.out.println("YAML 파일이 '" + outputFile + "'에 저장되었습니다.");
//		} catch (IOException e) {
//			System.out.println("Error writing YAML file " + outputFile + ": " + e.getMessage());
//		}
//	}
//
//	// parse 메서드 구현
//	public static List<APIEntry> parse(String yamlContentPath) {
//		List<APIEntry> apiEntries = loadApiYaml(yamlContentPath);
//		Map<String, List<String>> globalEntries = new HashMap<>();
//
//		Yaml yaml = new Yaml();
//		try (FileInputStream fis = new FileInputStream(yamlContentPath)) {
//			Object data = yaml.load(fis);
//			if (data instanceof List) {
//				List<?> yamlData = (List<?>) data;
//				for (Object item : yamlData) {
//					if (item instanceof Map) {
//						Map<?, ?> entryMap = (Map<?, ?>) item;
//						if (entryMap.containsKey("Global")) {
//							Object globalObj = entryMap.get("Global");
//							if (globalObj instanceof Map) {
//								Map<?, ?> globalMap = (Map<?, ?>) globalObj;
//								for (Map.Entry<?, ?> globalEntry : globalMap.entrySet()) {
//									String key = globalEntry.getKey().toString();
//									Object pathsObj = globalEntry.getValue();
//									List<String> paths = new ArrayList<>();
//									if (pathsObj instanceof List) {
//										for (Object path : (List<?>) pathsObj) {
//											paths.add(path.toString());
//										}
//									}
//									globalEntries.put(key, paths);
//								}
//							}
//						}
//					}
//				}
//			}
//		} catch (IOException e) {
//			System.out.println("Error reading YAML file " + yamlContentPath + ": " + e.getMessage());
//		} catch (Exception e) {
//			System.out.println("Error parsing YAML file: " + e.getMessage());
//		}
//
//		StringBuilder globalSrc = new StringBuilder();
//		for (Map.Entry<String, List<String>> entry : globalEntries.entrySet()) {
//			for (String path : entry.getValue()) {
//				globalSrc.append(loadSingleFileContents(path));
//			}
//		}
//
//		for (APIEntry entry : apiEntries) {
//			entry.setSrc(entry.getSrc() + loadFileContents(entry.getPaths()) + globalSrc.toString());
//		}
//
//		return apiEntries;
//	}
//
//	// 메인 메서드 예시
//	public static void main(String[] args) {
//		// 예시 YAML 파일 경로
//		String yamlPath = "input.yaml";
//
//		// parse 메서드 호출
//		List<APIEntry> entries = parse(yamlPath);
//
//		// 결과 출력
//		for (APIEntry entry : entries) {
//			System.out.println(entry);
//		}
//	}
//}
package com.hocs.server.openai.llm;

import static com.hocs.server.openai.util.FileManager.loadFileContents;
import static com.hocs.server.openai.util.FileManager.loadSingleFileContents;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.yaml.snakeyaml.Yaml;

// APIEntry 클래스 정의


public class ApiEntryMapper {



	// APIEntry 클래스 정의
	@Getter
	@Setter
	@ToString
	public static class APIEntry {

		private String API;
		private String method;
		private List<String> paths;
		private String src;
		private String globalSrc;
		private String absolutePath;

		// 기본 생성자
		public APIEntry() {
		}

		// 매개변수가 있는 생성자
		public APIEntry(String API, String method, List<String> paths, String src, String absolutePath) {
			this.API = API;
			this.method = method;
			this.paths = paths;
			this.src = src;
			this.absolutePath = absolutePath;
		}
	}


	// YAML 파일을 읽어 APIEntry 리스트로 변환하는 메서드
	private static List<APIEntry> loadApiYaml(String filePath) {
		List<APIEntry> apiEntries = new ArrayList<>();
		Yaml yaml = new Yaml();
		try (FileInputStream fis = new FileInputStream(filePath)) {
			Object data = yaml.load(fis);
			if (data instanceof List) {
				List<?> yamlData = (List<?>) data;
				for (Object item : yamlData) {
					if (item instanceof Map) {
						Map<?, ?> entryMap = (Map<?, ?>) item;
						if (entryMap.containsKey("API")) {
							String API = entryMap.get("API").toString();
							String method = entryMap.get("method").toString();
							List<String> paths = new ArrayList<>();
							Object pathsObj = entryMap.get("paths");
							if (pathsObj instanceof List) {
								for (Object path : (List<?>) pathsObj) {
									paths.add(path.toString());
								}
							}
							Object absolutePathObj = entryMap.get("absolutePath");
							APIEntry apiEntry = new APIEntry(API, method, paths, "", absolutePathObj.toString());
							apiEntries.add(apiEntry);
						}
					}
				}
			}
		} catch (IOException e) {
			System.out.println("Error reading YAML file " + filePath + ": " + e.getMessage());
		} catch (Exception e) {
			System.out.println("Error parsing YAML file: " + e.getMessage());
		}
		return apiEntries;
	}




	// parse 메서드 구현
	public static List<APIEntry> parse(String yamlContentPath) {
		List<APIEntry> apiEntries = loadApiYaml(yamlContentPath);
		Map<String, List<String>> globalEntries = new HashMap<>();

		Yaml yaml = new Yaml();
		try (FileInputStream fis = new FileInputStream(yamlContentPath)) {
			Object data = yaml.load(fis);
			if (data instanceof List) {
				List<?> yamlData = (List<?>) data;
				for (Object item : yamlData) {
					if (item instanceof Map) {
						Map<?, ?> entryMap = (Map<?, ?>) item;
						if (entryMap.containsKey("Global")) {
							Object globalObj = entryMap.get("Global");
							if (globalObj instanceof Map) {
								Map<?, ?> globalMap = (Map<?, ?>) globalObj;
								for (Map.Entry<?, ?> globalEntry : globalMap.entrySet()) {
									String key = globalEntry.getKey().toString();
									Object pathsObj = globalEntry.getValue();
									List<String> paths = new ArrayList<>();
									if (pathsObj instanceof List) {
										for (Object path : (List<?>) pathsObj) {
											paths.add(path.toString());
										}
									}
									globalEntries.put(key, paths);
								}
							}
						}
					}
				}
			}
		} catch (IOException e) {
			System.out.println("Error reading YAML file " + yamlContentPath + ": " + e.getMessage());
		} catch (Exception e) {
			System.out.println("Error parsing YAML file: " + e.getMessage());
		}

		StringBuilder globalSrc = new StringBuilder();
		for (Map.Entry<String, List<String>> entry : globalEntries.entrySet()) {
			for (String path : entry.getValue()) {
				globalSrc.append(loadSingleFileContents(path));
			}
		}

		for (APIEntry entry : apiEntries) {
			entry.setSrc(entry.getSrc() + loadFileContents(entry.getPaths()) + globalSrc.toString());
			entry.setGlobalSrc(globalSrc.toString());
		}

		return apiEntries;
	}

	// 메인 메서드 예시
	public static void main(String[] args) {
		// 예시 YAML 파일 경로
		String yamlPath = "input.yaml";

		// parse 메서드 호출
		List<APIEntry> entries = parse(yamlPath);

		// 결과 출력
		for (APIEntry entry : entries) {
			System.out.println(entry);
		}
	}
}