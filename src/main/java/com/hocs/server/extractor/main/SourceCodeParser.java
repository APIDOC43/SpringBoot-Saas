package com.hocs.server.extractor.main;

import com.hocs.server.extractor.ApiCodeExtractor;
import com.hocs.server.extractor.ClassifiedDataContainer;
import com.hocs.server.extractor.CodeCategorizer;
import com.hocs.server.extractor.CodeStructuresAnalyzer;
import com.hocs.server.extractor.config.ExtractorConfig;
import com.hocs.server.extractor.service.APISourceDependencyService;
import com.hocs.server.extractor.service.GitApiService;
import com.hocs.server.extractor.util.APISourceDependencyInfoMapper;
import com.hocs.server.extractor.util.FileManager;
import com.hocs.server.saas.user.gitapi.domin.GitRepo;
import com.hocs.server.openai.util.MemoryProcessPercentage;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SourceCodeParser {


	private final CodeCategorizer codeCategorizer;
	private final CodeStructuresAnalyzer codeStructuresAnalyzer;
	private final GitApiService gitApiService;
	private final APISourceDependencyService apiSourceDependencyService;


	public Path createMetaData(File PROJECT_ROOT_DIR, GitRepo gitRepo,String userId) throws Exception {
		List<Map<String, Object>> outputData = new ArrayList<>(); // 출력 결과를 저장할 리스트
		String SOURCE_ROOT = new File(PROJECT_ROOT_DIR, "src/main/java").getAbsolutePath();


		String gitrepoUrl = gitRepo.getUrl();
		String OUTPUT_FILE = new File(SOURCE_ROOT, "output-"+userId+".yaml").getAbsolutePath();

		//paser config 설정
		ExtractorConfig extractorConfig = new ExtractorConfig();
		extractorConfig.setConfig(SOURCE_ROOT);

		// 모든 Java 파일의 경로를 수집합니다.
		List<File> javaFiles = new ArrayList<>();

		codeStructuresAnalyzer.collectJavaFiles(new File(SOURCE_ROOT), javaFiles);

		// 각 타입별로 파일 경로를 매핑합니다.
		ClassifiedDataContainer classifiedDataContainer = codeCategorizer.parse(javaFiles);

		// API 단위로 필요한 파일 경로를 수집합니다.
		ApiCodeExtractor apiCodeExtractor = new ApiCodeExtractor(classifiedDataContainer);
		int done = 0;
		for (String controllerClassName : classifiedDataContainer.getControllerClasses()) {
			if(done == 3)
				break;
			MemoryProcessPercentage.save(userId,done++,3);
			String entryPath = classifiedDataContainer.getClassToFilePath()
				.get(controllerClassName);
			String entrySrcPath = entryPath.substring(entryPath.lastIndexOf("src"));

			String sourceCodeUrl = gitApiService.buildSourceCodeUrl(GitRepo.of(gitrepoUrl),
				entrySrcPath);
			apiCodeExtractor.traceControllerApis(sourceCodeUrl,controllerClassName,outputData);
		}

		// Global Dependencies를 출력 데이터에 추가
		addToGlobalDependenciesAtOutputData(classifiedDataContainer,outputData);

		// 출력 데이터를 YAML 파일로 저장합니다.
		FileManager.saveOutputAsYaml(outputData, OUTPUT_FILE);

		apiSourceDependencyService.save(
			APISourceDependencyInfoMapper.mapToAPISourceDependencyInfo(outputData, userId)
		);

		return Path.of(OUTPUT_FILE);
	}

	private void addToGlobalDependenciesAtOutputData(
		ClassifiedDataContainer classifiedDataContainer, List<Map<String, Object>> outputData) {
		if (!classifiedDataContainer.getGlobalDependencies().isEmpty()) {
			Map<String, Object> globalEntry = new LinkedHashMap<>();
			globalEntry.put("Global", new LinkedHashMap<>());
			Map<String, Object> globalMap = (Map<String, Object>) globalEntry.get("Global");

			for (Map.Entry<String, Set<String>> entry : classifiedDataContainer.getGlobalDependencies().entrySet()) {
				if (!entry.getValue().isEmpty()) {
					List<String> sortedPaths = new ArrayList<>(entry.getValue());
					Collections.sort(sortedPaths);
					globalMap.put(entry.getKey(), sortedPaths);
				}
			}

			outputData.add(globalEntry);
		}
	}
}
