package com.hocs.server.api_spec_generator.service;

import com.hocs.server.api_spec_generator.domain.output.PathItem;
import com.hocs.server.api_spec_generator.domain.output.Schema;
import com.hocs.server.api_spec_generator.llm.SpringAICommandForLLM;
import com.hocs.server.api_spec_generator.util.OpenAPIParser;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;


@Service
@RequiredArgsConstructor
public class OasIntegrationService {

	private final SpringAICommandForLLM springAiCommandForLLM;

	public HashMap<String, List<Schema>> schemaIntegration(ChatClient chatClient4o, Map<String, List<Schema>> schemasMap) {
		HashMap<String, List<Schema>> temp = new HashMap<>();
		for (String key : schemasMap.keySet()) {
			List<Schema> schemas = schemasMap.get(key);
			if (schemas.size() >= 2) {
//				temp.put(key, removeDuplicatesByLLM(chatClient4o, key, schemas).get(key));
			}
		}
		return temp;
	}

	private Map<String, List<Schema>> removeDuplicatesByLLM(ChatClient client, String key,
		List<Schema> schemas) {
		Map<String, List<Schema>> result = new HashMap<>();

		try {
			String integrationSchema = springAiCommandForLLM.integrationSchema(schemas, client);
			Schema schema = OpenAPIParser.parseToSchema(integrationSchema);
			ArrayList<Schema> temp = new ArrayList<>();
			temp.add(schema);
			result.put(key, temp);
			return result;
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
			return removeDuplicatesByLLM(client, key, schemas);
		}
	}

	public List<Map<String, PathItem>> pathIntegration(
		Map<String, List<Map<String, PathItem>>> pathList) {
		List<Map<String, PathItem>> integrationPaths = new ArrayList<>();
		for (String key : pathList.keySet()) {
			List<Map<String, PathItem>> maps = pathList.get(key);
			if (maps.size() >= 2) {
				PathItem integrationPathitem = new PathItem();
				for (Map<String, PathItem> map : maps) {
					PathItem pathItem = map.get(key);
					if (pathItem.getX_link() != null) {
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
}
