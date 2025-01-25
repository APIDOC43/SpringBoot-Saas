package com.hocs.server.api_doc_pipline.service;

import com.hocs.server.api_doc_pipline.domain.ApiInfoInPipline;
import com.hocs.server.api_doc_pipline.domain.ControllerFile;
import com.hocs.server.api_doc_pipline.service.out.port.ApiEndpointCollectorPortInPipline;
import com.hocs.server.openai.domain.input.APIMetadata;
import com.hocs.server.openai.service.GenerateOasFacadeService;
import com.hocs.server.openai.util.HttpClient;
import com.hocs.server.saas.apidoc.service.impl.StaticApiDocServiceImpl;

import com.hocs.server.common.ProjectMetaData;
import com.hocs.server.saas_v2.domain.ApiInfo;
import java.io.File;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApiDocPiplineService {

	private final ApiEndpointCollectorPortInPipline apiEndpointCollectorPortInPipline;
	private final GenerateOasFacadeService llmService;
	private final StaticApiDocServiceImpl staticApiDocServiceImpl;

	public void execute(String userId, ProjectMetaData metaData, String[] filenamesRelatedException,
		String defaultBranchName, List<ApiInfo> excludeApiInfoInPipline) throws Exception {

		String srcRootPath = metaData.getSrcRootPath();

		//task 0;
		Map<ControllerFile, List<ApiInfoInPipline>> apiEndpointInfo = apiEndpointCollectorPortInPipline.findApiInfo(
			metaData.getCodingLanguage(),
			metaData.getProjectFramework(),
			metaData.getProjectRootPath(),
			100);

		for (ControllerFile controllerFile : apiEndpointInfo.keySet()) {
			//task 1
//			List<API> apis = dependencyAnalyzer.findDependency(controllerFile.getClassName())
			//CustomRAG 하나의 컨트롤러 파일에서 Endpoint별 API 정보를 추출합니다.
			List<APIMetadata> apiMetadata = apiEndpointCollectorPortInPipline.getApiEndpoints(
				userId, metaData, defaultBranchName, controllerFile);

			//task 2
			//LLM을 이용하여 OAS 데이터를 생성합니다.
			File cloneDir = metaData.getProjectRootPath().getToFile();
			llmService.generate(userId, apiMetadata, cloneDir);


			//task 3
			//OAS데이터를 렌더링합니다.
			HttpClient.toSaas(cloneDir, userId);


		}
	}
}
