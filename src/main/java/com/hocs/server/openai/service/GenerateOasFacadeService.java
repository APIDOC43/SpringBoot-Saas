package com.hocs.server.openai.service;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.hocs.server.openai.domain.input.APIEndpoint;
import com.hocs.server.openai.domain.output.PathAndComponents;
import com.hocs.server.openai.llm.SpringAICommandForLLM;
import com.hocs.server.openai.llm.exception.ApiEntriesNullException;
import com.hocs.server.openai.repository.OasRepository;
import com.hocs.server.openai.util.FileManager;
import com.hocs.server.openai.util.MemoryProcessPercentage;
import com.hocs.server.openai.domain.output.Components;
import com.hocs.server.openai.domain.output.OAS;
import com.hocs.server.openai.domain.output.OasInfo;
import com.hocs.server.openai.domain.output.PathItem;
import com.hocs.server.openai.domain.output.Schema;
import java.io.File;
import java.io.IOException;
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

@Service
@RequiredArgsConstructor
public class GenerateOasFacadeService {


	private final SpringAICommandForLLM springAiCommandForLLM;
	private final ExceptionFormatService exceptionFormatService;
	private final OasIntegrationService oasIntegrationService;
	private final OasRepository OasRepository;

	public void generate(String userId, List<APIEndpoint> apiEndpoints, File projectDir) throws IOException {
		String projectRootPath = projectDir.getAbsolutePath();

		ChatClient client = springAiCommandForLLM.createChatClient4o();

		/** output.yaml to APIEndpoint **/
		if (apiEndpoints == null || apiEndpoints.size() == 0) {
			throw new ApiEntriesNullException("APIEndpoint is empty");
		}

		String exceptionFormatSrc = exceptionFormatService.findRelatedExceptionSrc(projectRootPath, client);

		Map<String, List<Schema>> schemasMap = new HashMap<>();
		Map<String, List<Map<String, PathItem>>> pathList = new HashMap<>();

		int totalTasks = Math.min(apiEndpoints.size(), 3); // 작업 개수 제한
		AtomicInteger completedTasks = new AtomicInteger(0); // 완료된 작업 수

		// CompletableFuture 리스트를 생성하고 병렬 실행
		//현재 apiEndpoint 하나에 대한 소스파일에는 여러개의 endpoint가 담겨있음. extartor에서 하나의 엔드포인트에는 해당하는 소스부분만 잘라서 보내줘야함.
		//위 과정이 이루어지면. 중복은 발생하지 않고 merge과정은 필요 없어짐.
		List<CompletableFuture<Void>> futures = apiEndpoints.stream()
			.limit(totalTasks) // 처음 3개 항목에 대해서만 병렬 처리
			.map(apiEndpoint -> CompletableFuture.runAsync(() ->
				generateOasPathSchemaSnippet(client, apiEndpoint, schemasMap, pathList, exceptionFormatSrc)
			).thenRun(() -> { // 작업 완료 후 실행
				int completed = completedTasks.incrementAndGet();
				MemoryProcessPercentage.save(userId, completed, totalTasks); // 진행 상황 계산
			}))
			.collect(Collectors.toList());

		// 모든 CompletableFuture 완료 대기
		CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

		//path integeration
		List<Map<String, PathItem>> integrationPaths = oasIntegrationService.pathIntegration(pathList);
		//schema integration
		Map<String, List<Schema>> integrationSchemaMap = oasIntegrationService.schemaIntegration(client, schemasMap);

		//OAS 객체로 다루게 되면 아래 과정은 필요없음.
		String result = merge(schemasMap, integrationPaths);

		FileManager.saveToFile(result, projectRootPath + "/output_file-fix.yaml");

		OAS oas = OAS.create(
			userId,
			OasInfo.create(userId, "", "", "", "", "3.0.1"),
			pathList,
			integrationSchemaMap);

		OasRepository.save(oas);



	}



	private String merge(Map<String, List<Schema>> schemasMap,
		List<Map<String, PathItem>> integrationPaths) {
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

	private void generateOasPathSchemaSnippet(ChatClient client, APIEndpoint apiEndpoint,
		Map<String, List<Schema>> schemasMap,
		Map<String, List<Map<String, PathItem>>> pathList, String exceptionFormatSrc) {


		PathAndComponents pathAndComponents = oasApiSnippet(client, apiEndpoint, exceptionFormatSrc);
		pathAndComponents.getPaths().values().forEach(f -> f.setX_link(apiEndpoint.getAbsolutePath()));

		Components components = pathAndComponents.getComponents();
		if (components != null && components.getSchemas() != null) {
			components.getSchemas().forEach((key, schema) -> {
				schemasMap.putIfAbsent(key, new ArrayList<>());
				schemasMap.get(key).add(schema);
			});
		}

		Map<String, PathItem> paths = pathAndComponents.getPaths();
		paths.forEach((key, pathItem) -> {
			pathList.putIfAbsent(key, new ArrayList<>());
			pathList.get(key).add(paths);
		});
	}



	private PathAndComponents oasApiSnippet(ChatClient client, APIEndpoint apiEndpoint, String exceptionFormatSrc) {

		PathAndComponents pathAndComponents = null;
		try {
			pathAndComponents = springAiCommandForLLM.requestOasApiSnippet(client, apiEndpoint, 0,
				exceptionFormatSrc);
		} catch (JsonProcessingException e) {
			sleep(3000);
			return oasApiSnippet(client, apiEndpoint, exceptionFormatSrc);
		}
		return pathAndComponents;
	}

	private void sleep(int time) {
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}


}