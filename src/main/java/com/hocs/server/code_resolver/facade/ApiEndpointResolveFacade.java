package com.hocs.server.code_resolver.facade;

import com.hocs.server.code_resolver.domain.APIEntries;
import com.hocs.server.api_doc_pipline.domain.ControllerFile;
import com.hocs.server.code_resolver.service.ApiEndpointCollectorService;
import com.hocs.server.code_resolver.service.ApiInfoExtractorService;
import com.hocs.server.common.ProjectMetaData;
import com.hocs.server.extractor.core.DependencyAnalyzer;
import com.hocs.server.extractor.core.SrcFileCollector;
import com.hocs.server.extractor.core.config.ExtractorConfig;
import com.hocs.server.extractor.core.data.JavaClassifiedDataContainer;
import com.hocs.server.extractor.core.data.JavaClassifiedDataGenerator;
import com.hocs.server.extractor.domain.API;
import com.hocs.server.extractor.domain.APISourceDependencyInfo;
import com.hocs.server.extractor.domain.ClientProjectType;
import com.hocs.server.extractor.domain.GlobalSourceDependency;
import com.hocs.server.openai.domain.input.APIMetadata;
import com.hocs.server.saas.demo.mapper.APISourceDependencyInfoToAPIEndpoint;
import com.hocs.server.saas_v2.common.annotation.Facade;
import com.hocs.server.saas_v2.domain.ApiInfo;
import com.hocs.server.saas_v2.service.out.ApiEndpointCollector.adapter.FindApiInfoApiRequest;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;


@Facade
@RequiredArgsConstructor
public class ApiEndpointResolveFacade {

	private final ApiInfoExtractorService ApiInfoExtractorService;
	private final ApiEndpointCollectorService apiEndpointCollectorService;
	private final DependencyAnalyzer dependencyAnalyzer;
	private final SrcFileCollector srcFileCollector;
	private final JavaClassifiedDataGenerator javaCodeCategorizer;

	public Map<ControllerFile, List<ApiInfo>> findApiInfo(FindApiInfoApiRequest request) {
		APIEntries apiEntries = apiEndpointCollectorService.findControllerFiles(
			request.getLanguage(), request.getProjectFramework(), request.getPath());

		List<File> controllerFiles = apiEntries.getFiles();

		return ApiInfoExtractorService.extractApiInfo(controllerFiles);
	}

	public List<APIMetadata> findAPIMetadata(String userId, ProjectMetaData metaData,
		String defaultBranchName, ControllerFile controllerFile) {

		JavaClassifiedDataContainer container = initJavaClassifiedDataContainer(
			metaData.getProjectRootPath().getPath());
		List<API> apis = null;
		try {
			apis = dependencyAnalyzer.findDependency(controllerFile.getClassName());


		for (API api : apis) {
			String gitCloneUrl = metaData.getGitCloneUrl();
			if (gitCloneUrl.endsWith(".git")) {
				gitCloneUrl = gitCloneUrl.split("\\.")[0];
			}

			api.setLink(
				gitCloneUrl + "/blob/" + defaultBranchName + "/" + controllerFile.getPath());
		}

		GlobalSourceDependency globalSourceDependency = container.getGlobalDependencies(userId);
		APISourceDependencyInfo apiSourceDependencyInfo = APISourceDependencyInfo
			.create(UUID.randomUUID().toString(), userId, apis, globalSourceDependency);
		//repository.save(apiSourceDependencyInfo);
		List<APIMetadata> apiMetadata = APISourceDependencyInfoToAPIEndpoint
			.mapToAPIEndpoint(apiSourceDependencyInfo);

		return apiMetadata;

		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

	private JavaClassifiedDataContainer initJavaClassifiedDataContainer(Path clonedDir) {
		Path SOURCE_ROOT = new File(clonedDir.toFile(),
			ClientProjectType.SPRING_JAVA.srcRootPath()).toPath();
		String SOURCE_ROOT_STR = SOURCE_ROOT.toString();

		//paser config 설정
		ExtractorConfig extractorConfig = new ExtractorConfig();
		extractorConfig.setConfig(SOURCE_ROOT_STR);

		// 모든 Java 파일의 경로를 수집합니다.
		List<File> files = srcFileCollector.collectFiles(new File(SOURCE_ROOT_STR),
			ClientProjectType.SPRING_JAVA.srcSuffix());

		// 각 타입별로 파일 경로를 매핑합니다.
		try {
			return javaCodeCategorizer.init(files);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}