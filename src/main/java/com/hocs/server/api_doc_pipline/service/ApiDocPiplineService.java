package com.hocs.server.api_doc_pipline.service;

import com.hocs.server.code_resolver.domain.ControllerFile;
import com.hocs.server.extractor.core.DependencyAnalyzer;
import com.hocs.server.extractor.core.SrcFileCollector;
import com.hocs.server.extractor.core.config.ExtractorConfig;
import com.hocs.server.extractor.core.data.JavaClassifiedDataContainer;
import com.hocs.server.extractor.core.data.JavaClassifiedDataGenerator;
import com.hocs.server.extractor.domain.API;
import com.hocs.server.extractor.domain.APISourceDependencyInfo;
import com.hocs.server.extractor.domain.ClientProjectType;
import com.hocs.server.extractor.domain.GlobalSourceDependency;
import com.hocs.server.openai.domain.input.APIEndpoint;
import com.hocs.server.openai.service.GenerateOasFacadeService;
import com.hocs.server.openai.util.HttpClient;
import com.hocs.server.openai.util.MemoryProcessPercentage;
import com.hocs.server.saas.apidoc.service.impl.StaticApiDocServiceImpl;
import com.hocs.server.saas.demo.mapper.APISourceDependencyInfoToAPIEndpoint;
import com.hocs.server.saas_v2.domain.ApiInfo;
import com.hocs.server.saas_v2.domain.ProjectMetaData;
import com.hocs.server.saas_v2.service.out.ApiEndpointCollector.port.ApiEndpointCollectorPort;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApiDocPiplineService {

	private final ApiEndpointCollectorPort apiEndpointCollectorPort;
	private final GenerateOasFacadeService llmService;
	private final SrcFileCollector srcFileCollector;
	private final JavaClassifiedDataGenerator javaCodeCategorizer;
	private final DependencyAnalyzer dependencyAnalyzer;
	private final StaticApiDocServiceImpl staticApiDocServiceImpl;

	public void execute(String userId, ProjectMetaData metaData, String[] filenamesRelatedException,
		String defaultBranchName, List<ApiInfo> excludeApiInfo) throws Exception {

		String srcRootPath = metaData.getSrcRootPath();

		JavaClassifiedDataContainer container = initJavaClassifiedDataContainer(
			metaData.getProjectRootPath().getPath());

		Map<ControllerFile, List<ApiInfo>> apiEndpointInfo = apiEndpointCollectorPort.findApiInfo(
			metaData.getCodingLanguage(),
			metaData.getProjectFramework(),
			metaData.getProjectRootPath(),
			100);



		for (ControllerFile controllerFile : apiEndpointInfo.keySet()) {

			//task 1
			List<API> apis = dependencyAnalyzer.findDependency(controllerFile.getClassName());

			for (API api : apis) {
				String gitCloneUrl = metaData.getGitCloneUrl();
				if(gitCloneUrl.endsWith(".git"))
					gitCloneUrl = gitCloneUrl.split("\\.")[0];

				api.setLink(gitCloneUrl+"/blob/"+defaultBranchName+"/"+controllerFile.getPath());
			}

			GlobalSourceDependency globalSourceDependency = container.getGlobalDependencies(userId);
			APISourceDependencyInfo apiSourceDependencyInfo = APISourceDependencyInfo
				.create(UUID.randomUUID().toString(),userId,apis,globalSourceDependency);
			//repository.save(apiSourceDependencyInfo);
			List<APIEndpoint> apiEndpoints = APISourceDependencyInfoToAPIEndpoint
				.mapToAPIEndpoint(apiSourceDependencyInfo);

			//task 2
			File cloneDir = metaData.getProjectRootPath().getPath().toFile();
			llmService.generate(userId,apiEndpoints, cloneDir);
			MemoryProcessPercentage.clear(userId);

			//task 3
			HttpClient.toSaas(cloneDir, userId);

//			List<FilesData> htmlFiles = staticApiDocServiceImpl.findApiListByUserId(userId);
//			model.addAttribute("htmlFiles", htmlFiles);
//			String response = HttpClient.findHtmlRequest(htmlFiles.get(0).getFilePath());
//			model.addAttribute("content", response);

			//task 3
		}





	}
	private JavaClassifiedDataContainer initJavaClassifiedDataContainer(Path clonedDir)
		throws IOException {
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
		return javaCodeCategorizer.init(files);
	}
}
