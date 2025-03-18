package com.hocs.server.code_parser.facade;

import com.hocs.server.code_parser.core.config.ExtractorConfig;
import com.hocs.server.code_parser.core.dataobject.JavaClassifiedDataContainer;
import com.hocs.server.code_parser.core.dataobject.JavaClassifiedDataGenerator;
import com.hocs.server.code_parser.core.domain.API;
import com.hocs.server.code_parser.core.domain.APISourceDependencyInfo;
import com.hocs.server.code_parser.core.domain.ClientProjectType;
import com.hocs.server.code_parser.core.domain.GlobalSourceDependency;
import com.hocs.server.code_parser.core.service.DependencyAnalyzer;
import com.hocs.server.code_parser.core.service.SrcFileCollector;
import com.hocs.server.code_parser.domain.APIEntries;
import com.hocs.server.code_parser.service.ApiEndpointCollectorService;
import com.hocs.server.code_parser.service.ApiExcludeService;
import com.hocs.server.code_parser.service.ApiSourceDependencyBatchSaveService;
import com.hocs.server.common.domain.ApiInfo;
import com.hocs.server.common.domain.ProjectMetaData;
import com.hocs.server.pipline_orchestrator.domain.ControllerFile;
import com.hocs.server.saas_platform.common.annotation.Facade;
import com.hocs.server.saas_platform.service.external.ApiEndpointCollector.adapter.FindApiInfoApiRequest;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Facade
@RequiredArgsConstructor
@Slf4j
public class ApiEndpointResolveFacade {

	private final ApiExcludeService ApiExcludeService;
	private final ApiEndpointCollectorService apiEndpointCollectorService;
	private final ApiSourceDependencyBatchSaveService apiSourceDependencyBatchSaveService;
	private final DependencyAnalyzer dependencyAnalyzer;
	private final SrcFileCollector srcFileCollector;
	private final JavaClassifiedDataGenerator javaCodeCategorizer;

	public Map<ControllerFile, List<ApiInfo>> findApiInfo(FindApiInfoApiRequest request) {
		APIEntries apiEntries = apiEndpointCollectorService.findControllerFiles(
			request.getLanguage(), request.getProjectFramework(), request.getPath());

		List<File> controllerFiles = apiEntries.getFiles();

		return ApiExcludeService.excludeApi(controllerFiles, request.getExcludeFile());
	}

	public APISourceDependencyInfo findAPIMetadata(String userId, ProjectMetaData metaData,
		String defaultBranchName, ControllerFile controllerFile, String requestId) {
		JavaClassifiedDataContainer container = initJavaClassifiedDataContainer(
			metaData.getProjectRootPath().getPath());

		List<API> apis = null;
		try {

			//사용자 프로젝트 언어 및 프레임워크에 따라 달라집니다.
			//if(metaData.getProjectFramework().equals(ProjectFramework.SPRINGBOOT))
			apis = dependencyAnalyzer.findDependency(controllerFile.getClassName(), container);

			for (API api : apis) {
				String gitCloneUrl = metaData.getGitRepoData().getCloneUrl();
				if (gitCloneUrl.endsWith(".git")) {
					gitCloneUrl = gitCloneUrl.split("\\.")[0];
				}

				api.setLink(
					gitCloneUrl + "/blob/" + defaultBranchName + "/" + controllerFile.getPath());
			}

			GlobalSourceDependency globalSourceDependency = container.getGlobalDependencies(userId);
			APISourceDependencyInfo apiSourceDependencyInfo = APISourceDependencyInfo
				.create(requestId, userId, apis, globalSourceDependency);

			apiSourceDependencyBatchSaveService.addEntity(apiSourceDependencyInfo);

			return apiSourceDependencyInfo;

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