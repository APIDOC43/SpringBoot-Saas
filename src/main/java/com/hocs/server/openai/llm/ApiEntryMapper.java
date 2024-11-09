package com.hocs.server.openai.llm;

import static com.hocs.server.openai.util.FileManager.loadFileContents;
import static com.hocs.server.openai.util.FileManager.loadSingleFileContents;

import com.hocs.server.openai.domain.APIEntry;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.yaml.snakeyaml.Yaml;

// APIEntry 클래스 정의
public class ApiEntryMapper {

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
							APIEntry apiEntry = new APIEntry(API, method, paths, absolutePathObj.toString());
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

}