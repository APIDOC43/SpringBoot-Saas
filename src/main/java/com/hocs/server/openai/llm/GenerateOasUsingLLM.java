package com.hocs.server.openai.llm;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.hocs.server.saas.model.Components;
import com.hocs.server.saas.model.OpenAPI;
import com.hocs.server.saas.model.PathItem;
import com.hocs.server.saas.model.Schema;
import com.hocs.server.openai.llm.ApiEntryMapper.APIEntry;
import com.hocs.server.openai.llm.exception.ApiEntriesNullException;
import com.hocs.server.openai.util.FileManager;
import com.hocs.server.openai.util.MemoryProcessPercentage;
import com.hocs.server.util.OpenAPIParser;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;

@Service
@RequiredArgsConstructor
public class GenerateOasUsingLLM {


	private final SpringAICommandForLLM springAiCommandForLLM;

	public void generate(String userId,Path metaDataFilePath, File projectDir) throws IOException {
		String projectRootPath = projectDir.getAbsolutePath();
		String sourceCodeMetaData = metaDataFilePath.toFile().getAbsolutePath();

		ChatClient client = springAiCommandForLLM.createChatClient4o();
		ChatClient chatClient4o = springAiCommandForLLM.createChatClient4o();

		/** output.yaml to APIEntry **/
		List<APIEntry> apiEntries = ApiEntryMapper.parse(sourceCodeMetaData);
		if (apiEntries == null || apiEntries.size() == 0) {
			throw new ApiEntriesNullException("APIEntry is empty");
		}

		Map<String, List<Schema>> schemasMap = new HashMap<>();
		Map<String, List<Map<String, PathItem>>> pathList = new HashMap<>();

		String exceptionFormatSrc = findRelatedExceptionSrc(projectRootPath, chatClient4o);

		int totalTasks = Math.min(apiEntries.size(), 3); // 작업 개수 제한
		AtomicInteger completedTasks = new AtomicInteger(0); // 완료된 작업 수

		// CompletableFuture 리스트를 생성하고 병렬 실행
		List<CompletableFuture<Void>> futures = apiEntries.stream()
			.limit(totalTasks) // 처음 3개 항목에 대해서만 병렬 처리
			.map(apiEntry -> CompletableFuture.runAsync(() ->
				processApiEntry(client, apiEntry, schemasMap, pathList,exceptionFormatSrc)
			).thenRun(() -> { // 작업 완료 후 실행
				int completed = completedTasks.incrementAndGet();
				MemoryProcessPercentage.save(userId,completed,totalTasks); // 진행 상황 계산
			}))
			.collect(Collectors.toList());

		// 모든 CompletableFuture 완료 대기
		CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

		//path integeration
		List<Map<String, PathItem>> integrationPaths = pathIntegration(pathList);
		//schema integration
		removeDuplicates(chatClient4o, schemasMap);

		String result = merge(schemasMap, integrationPaths);

		FileManager.saveToFile(result, projectRootPath + "/output_file-fix.yaml");

	}

	private String findRelatedExceptionSrc(String projectRootPath, ChatClient chatClient4o) throws IOException {
		String[] ExceptionSrc = springAiCommandForLLM.findFilePathRelatedExceptionFormatSrc(
			projectRootPath, chatClient4o);

		StringBuffer sb = new StringBuffer();
		for (String src : ExceptionSrc) {
			sb.append(new String(Files.readAllBytes(Paths.get(src)))).append("\n");
		}
		return sb.toString();
	}

	private void removeDuplicates(ChatClient chatClient4o, Map<String, List<Schema>> schemasMap) {
		for (String key : schemasMap.keySet()) {
			List<Schema> schemas = schemasMap.get(key);
			if (schemas.size() >= 2) {
				removeDuplicatesByLLM(chatClient4o, schemasMap, key, schemas);
			}
		}

	}

	private static List<Map<String, PathItem>> pathIntegration(
		Map<String, List<Map<String, PathItem>>> pathList) {
		List<Map<String, PathItem>> integrationPaths = new ArrayList<>();
		for (String key : pathList.keySet()) {
			List<Map<String, PathItem>> maps = pathList.get(key);
			if (maps.size() >= 2) {
				PathItem integrationPathitem = new PathItem();
				for (Map<String, PathItem> map : maps) {
					PathItem pathItem = map.get(key);
					if(pathItem.getX_link() != null){
						integrationPathitem.setX_link(pathItem.getX_link());
					}
					if (pathItem.getGet() != null) {
						integrationPathitem.setGet(pathItem.getGet());
					}
					if (pathItem.getHead() != null) {
						integrationPathitem.setHead(pathItem.getHead());
					}
					if (pathItem.getPatch() != null) {
						integrationPathitem.setPatch(pathItem.getPatch());
					}
					if (pathItem.getPut() != null) {
						integrationPathitem.setPut(pathItem.getPut());
					}
					if (pathItem.getPost() != null) {
						integrationPathitem.setPost(pathItem.getPost());
					}
					if (pathItem.getOptions() != null) {
						integrationPathitem.setOptions(pathItem.getOptions());
					}
					if (pathItem.getDelete() != null) {
						integrationPathitem.setDelete(pathItem.getDelete());
					}
					if (pathItem.getExtensions() != null) {
						integrationPathitem.setExtensions(pathItem.getExtensions());
					}
					if (pathItem.getTrace() != null) {
						integrationPathitem.setTrace(pathItem.getTrace());
					}
				}
				Map<String, PathItem> stringPathItemHashMap = new HashMap<>();
				stringPathItemHashMap.put(key, integrationPathitem);
				integrationPaths.add(stringPathItemHashMap);
			} else {
				integrationPaths.add(maps.get(0));
			}
		}
		return integrationPaths;
	}

	private String merge(Map<String, List<Schema>> schemasMap, List<Map<String, PathItem>> integrationPaths) {
		ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
		objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
		StringBuffer sb;
		sb = new StringBuffer();
		sb.append("""
			openapi: 3.0.1
			info:
			  title: 플젝 제목
			  description: 플젝 설명
			  version: 버전
			""").append("\n");
		sb.append("paths:").append("\n");
		// 객체를 YAML로 직렬화
		for (Map<String, PathItem> stringPathItemMap : integrationPaths) {

			try {
				String str = objectMapper.writeValueAsString(stringPathItemMap);
				str = str.replaceAll("(?m)^", "  ");
				sb.append(str).append("\n");
			} catch (JsonProcessingException e) {
				throw new RuntimeException(e);
			}
		}
		sb.append("components:").append("\n");
		sb.append("  ").append("schemas:").append("\n");
		for (String key : schemasMap.keySet()) {
			try {
				sb.append("    ").append(key).append(":").append("\n");
				String str = objectMapper.writeValueAsString(schemasMap.get(key).get(0));
				str = str.replaceAll("(?m)^", "      ");
				sb.append(str).append("\n");
			} catch (JsonProcessingException e) {
				throw new RuntimeException(e);
			}
		}
		String result = sb.toString().replace("---", "");
		return result;
	}

	private void processApiEntry(ChatClient client, APIEntry apiEntry,
		Map<String, List<Schema>> schemasMap,
		Map<String, List<Map<String, PathItem>>> pathList,String exceptionFormatSrc) {
		OpenAPI openAPI = oasApiSnippet(client, apiEntry,exceptionFormatSrc);
		openAPI.getPaths().values().forEach(f -> f.setX_link(apiEntry.getAbsolutePath()));

		Components components = openAPI.getComponents();
		if (components != null && components.getSchemas() != null) {
			components.getSchemas().forEach((key, schema) -> {
				schemasMap.putIfAbsent(key, new ArrayList<>());
				schemasMap.get(key).add(schema);
			});
		}

		Map<String, PathItem> paths = openAPI.getPaths();
		paths.forEach((key, pathItem) -> {
			pathList.putIfAbsent(key, new ArrayList<>());
			pathList.get(key).add(paths);
		});
	}

	private void removeDuplicatesByLLM(ChatClient client, Map<String, List<Schema>> schemasMap,
		String key,
		List<Schema> schemas) {
		try {
			String integrationSchema = springAiCommandForLLM.integrationSchema(schemas, client);
			Schema schema = OpenAPIParser.parseToSchema(integrationSchema);
			ArrayList<Schema> temp = new ArrayList<>();
			temp.add(schema);
			schemasMap.put(key, temp);
		} catch (ResourceAccessException e) {
			throw new ResourceAccessException(e.getMessage());
		} catch (Exception e) {
			if (e.getMessage().equals("TPM")) {
				throw new RuntimeException("TPM");
			}
			e.printStackTrace();
			try {
				Thread.sleep(5000);
			} catch (InterruptedException ex) {
				throw new RuntimeException(ex);
			}
			removeDuplicatesByLLM(client, schemasMap, key, schemas);
		}

	}



	private OpenAPI oasApiSnippet(ChatClient client, APIEntry apiEntry,String exceptionFormatSrc) {

		OpenAPI openAPI = null;
		try {
			openAPI = springAiCommandForLLM.requestOasApiSnippet(client, apiEntry, 0,exceptionFormatSrc);
		} catch (JsonProcessingException e) {
			sleep(3000);
			return oasApiSnippet(client, apiEntry,exceptionFormatSrc);
		}
		return openAPI;
	}

	private void sleep(int time) {
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}


}