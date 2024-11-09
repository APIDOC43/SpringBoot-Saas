package com.hocs.server.extractor.core.entry;

import com.hocs.server.extractor.core.DependencyAnalyzer;
import com.hocs.server.extractor.core.SrcFileCollector;
import com.hocs.server.extractor.core.config.ExtractorConfig;
import com.hocs.server.extractor.core.data.JavaClassifiedDataContainer;
import com.hocs.server.extractor.core.data.JavaClassifiedDataGenerator;
import com.hocs.server.extractor.domain.API;
import com.hocs.server.extractor.domain.APISourceDependencyInfo;
import com.hocs.server.extractor.domain.ClientProjectType;
import com.hocs.server.extractor.domain.GlobalSourceDependency;
import com.hocs.server.extractor.service.GitApiService;
import com.hocs.server.openai.util.MemoryProcessPercentage;
import com.hocs.server.saas.user.gitapi.domin.GitRepo;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * API Source Dependency Client
 * 역할 : 하나의 API가 실행될 때 Source Dependency 정보를 추출하는 역할을 수행
 */
@Service
@RequiredArgsConstructor
public class SpringJavaApiCodeClient {


	private final JavaClassifiedDataGenerator javaCodeCategorizer;
	private final SrcFileCollector srcFileCollector;
	private final GitApiService gitApiService;
	private final DependencyAnalyzer dependencyAnalyzer;


	/**
	 * @param SOURCE_ROOT_DIR
	 * @param gitRepo
	 * @param userId
	 * @return APISourceDependencyInfo Filee Path
	 * @throws Exception
	 */
	public APISourceDependencyInfo findDependencyInfo(ClientProjectType clientProjectType,Path SOURCE_ROOT_DIR, GitRepo gitRepo,String userId)
		throws Exception {
		String SOURCE_ROOT_STR = SOURCE_ROOT_DIR.toString();

		//paser config 설정
		ExtractorConfig extractorConfig = new ExtractorConfig();
		extractorConfig.setConfig(SOURCE_ROOT_STR);

		// 모든 Java 파일의 경로를 수집합니다.
		List<File> files = srcFileCollector.collectFiles(new File(SOURCE_ROOT_STR), clientProjectType.srcSuffix());

		// 각 타입별로 파일 경로를 매핑합니다.
		JavaClassifiedDataContainer dataContainer = javaCodeCategorizer.init(files);

		// API 단위로 필요한 파일 경로를 수집합니다.
		List<API> apis = new ArrayList<>();

		int limit = 0;
		for (String controllerClassName : dataContainer.getControllerClasses()) {
			if(limit == 3)
				break;

			MemoryProcessPercentage.save(userId,limit++,3);

			List<API> api = dependencyAnalyzer.findDependency(controllerClassName);

			// Controller 클래스의 소스 코드 github 링크를 생성합니다.
			addGithubLink(gitRepo, dataContainer, controllerClassName, api);

			apis.addAll(api);
		}

		// Global Dependencies를 출력 데이터에 추가
		GlobalSourceDependency globalSourceDependency = dataContainer.getGlobalDependencies(userId);

		return APISourceDependencyInfo.create(UUID.randomUUID().toString(), userId, apis, globalSourceDependency);

	}

	/**
	 * Controller 클래스의 소스 코드 github 링크를 생성합니다.
	 */
	private void addGithubLink(GitRepo gitRepo, JavaClassifiedDataContainer javaClassifiedDataContainer,
		String controllerClassName, List<API> api) {
		String entryPath = javaClassifiedDataContainer.getClassToFilePath()
			.get(controllerClassName);
		String entrySrcPath = entryPath.substring(entryPath.lastIndexOf("src"));
		String sourceCodeLink = gitApiService.buildSourceCodeUrl(gitRepo, entrySrcPath);

		for (API api1 : api) {
			api1.setLink(sourceCodeLink);
		}
	}



}
