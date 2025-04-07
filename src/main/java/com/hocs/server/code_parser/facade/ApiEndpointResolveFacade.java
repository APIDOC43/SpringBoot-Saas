package com.hocs.server.code_parser.facade;

import com.hocs.server.code_parser.core.dataobject.JavaClassifiedDataContainer;
import com.hocs.server.code_parser.core.dataobject.JavaClassifiedStore;
import com.hocs.server.code_parser.core.domain.API;
import com.hocs.server.code_parser.core.domain.APISourceDependencyInfo;
import com.hocs.server.code_parser.core.domain.GlobalSourceDependency;
import com.hocs.server.code_parser.core.service.DependencyAnalyzer;
import com.hocs.server.code_parser.domain.APIEntries;
import com.hocs.server.code_parser.repository.APISourceDependencyRepository;
import com.hocs.server.code_parser.service.ApiEndpointCollectorService;
import com.hocs.server.code_parser.service.ApiExcludeService;
import com.hocs.server.code_parser.service.ApiSourceDependencyBatchSaveService;
import com.hocs.server.common.domain.ApiInfo;
import com.hocs.server.common.domain.ProjectFramework;
import com.hocs.server.common.domain.ProjectMetaData;
import com.hocs.server.pipline_orchestrator.domain.ControllerFile;
import com.hocs.server.pipline_orchestrator.ratelimit.PipelineTask;
import com.hocs.server.saas_platform.common.annotation.Facade;
import com.hocs.server.saas_platform.service.external.ApiEndpointCollector.adapter.FindApiInfoApiRequest;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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


	public Map<ControllerFile, List<ApiInfo>> findApiInfo(FindApiInfoApiRequest request) {
		APIEntries apiEntries = apiEndpointCollectorService.findControllerFiles(
			request.getLanguage(), request.getProjectFramework(), request.getPath());

		List<File> controllerFiles = apiEntries.getFiles();

		return ApiExcludeService.excludeApi(controllerFiles, request.getExcludeFile());
	}

	public APISourceDependencyInfo  findAPIMetadataByTask(String userId, ProjectMetaData metaData,
		String defaultBranchName, PipelineTask task, String requestId) {
		JavaClassifiedDataContainer container = JavaClassifiedStore.get(task.getRequestId());

		List<API> apis = new ArrayList<>();
		try {
			//사용자 프로젝트 언어 및 프레임워크에 따라 달라집니다. TODO:책임분리
			if(metaData.getProjectFramework().equals(ProjectFramework.SPRINGBOOT)) {
				apis.add(
					dependencyAnalyzer.findDependencyByTask(task.getControllerFile().getClassName(),
						task, container)
				);
			}
		} catch (Exception e) {
			e.printStackTrace();
			log.info("dependencyAnalyzer.findDependency Exception");
			throw new RuntimeException(e);
		}


		String gitCloneUrl = metaData.getGitRepoData().getCloneUrl();
		if (gitCloneUrl.endsWith(".git")) {
			gitCloneUrl = gitCloneUrl.split("\\.")[0];
		}

		for (API api : apis) {
			api.setLink(
				gitCloneUrl + "/blob/" + defaultBranchName + "/" + task.getControllerFile().getPath());
		}

		GlobalSourceDependency globalSourceDependency = container.getGlobalDependencies(userId);
		APISourceDependencyInfo apiSourceDependencyInfo = APISourceDependencyInfo
			.create(requestId, UUID.randomUUID().toString(), userId, apis, globalSourceDependency);

		try {
			apiSourceDependencyBatchSaveService.addEntity(apiSourceDependencyInfo);
		}catch (InterruptedException e){
			throw new RuntimeException("Batch save InterruptedException");
		}

		return apiSourceDependencyInfo;
	}
}