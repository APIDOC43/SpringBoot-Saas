package com.hocs.server.pipline_orchestrator.service;

import com.hocs.server.api_spec_generator.domain.input.APIMetadata;
import com.hocs.server.api_spec_generator.service.GenerateOasFacadeService;
import com.hocs.server.common.domain.ApiInfo;
import com.hocs.server.common.domain.ProjectMetaData;
import com.hocs.server.pipline_orchestrator.domain.ApiInfoInPipline;
import com.hocs.server.pipline_orchestrator.domain.ControllerFile;
import com.hocs.server.pipline_orchestrator.service.out.OasSendClient;
import com.hocs.server.pipline_orchestrator.service.out.port.ApiEndpointCollectorPortInPipline;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApiDocPipelineService {
	private final ApiEndpointCollectorPortInPipline apiEndpointCollectorPortInPipline;
	private final GenerateOasFacadeService llmService; //internal call로 분리
	private final OasSendClient oasSendClient;

	//deprecated
	public void execute(
		String userId,
		ProjectMetaData metaData,
		String[] filenamesRelatedException,
		String defaultBranchName,
		List<ApiInfo> excludeApiInfoInPipline) throws Exception
	{
		String requestId = UUID.randomUUID().toString();

		//task 0;
		Map<ControllerFile, List<ApiInfoInPipline>> apiEndpointInfo = apiEndpointCollectorPortInPipline.findApiInfo(
			metaData.getCodingLanguage(),
			metaData.getProjectFramework(),
			metaData.getProjectRootPath(),
			excludeApiInfoInPipline,
			100);

		for (ControllerFile controllerFile : apiEndpointInfo.keySet()) {
			//task 1
			//CustomRAG 하나의 컨트롤러 파일에서 Endpoint별 API 정보를 추출합니다.
			List<APIMetadata> apiMetadata = apiEndpointCollectorPortInPipline.getApiEndpoints(
				userId, metaData, defaultBranchName, controllerFile, requestId);

			//task 2
			//LLM을 이용하여 OAS 데이터를 생성합니다.
			File cloneDir = metaData.getProjectRootPath().getToFile();
			llmService.generate(userId, apiMetadata, cloneDir);

			//task 3
			//OAS데이터를 렌더링합니다.
			oasSendClient.toSaas(cloneDir, userId);
		}
	}

	@Async("ExternalAsyncExecutor")
	public void executeAsync(
		String userId, ProjectMetaData metaData,
		String[] filenamesRelatedException,
		String defaultBranchName,
		List<ApiInfo> excludeApiInfoInPipline) {
		//레포정보기반으로 requestID전달 받도록 수정해야됨. 임시적을로 여기서 생성
		String requestId = UUID.randomUUID().toString();

		Map<ControllerFile, List<ApiInfoInPipline>> apiEndpointInfo = apiEndpointCollectorPortInPipline.findApiInfo(
			metaData.getCodingLanguage(),
			metaData.getProjectFramework(),
			metaData.getProjectRootPath(),
			excludeApiInfoInPipline, 100);

		File cloneDir = metaData.getProjectRootPath().getToFile();
		List<CompletableFuture<Void>> futures = new ArrayList<>();

		for (ControllerFile controllerFile : apiEndpointInfo.keySet()) {
			CompletableFuture<Void> future =
				getApiEndpoint(
					userId,
					metaData,
					defaultBranchName,
					requestId,
					controllerFile
				)

					//task 2 : API Endpoint 하나씩 LLM을 통해 명세를 생성합니다.
					.thenCompose( apiMetaDatas ->
						generateApiSpec(
							userId,
							filenamesRelatedException,
							requestId,
							cloneDir,
							apiMetaDatas
						)
					);

			futures.add(future);
		}

		// 모든 비동기 작업이 완료될 때까지 대기합니다.
		CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
			.exceptionally(this::handleFinalJoinException)  // 최종 예외 전파 방지
			.join();

		// Task 3: OAS 데이터를 렌더링합니다.
		oasSendClient.toSaas(cloneDir, userId);
	}


	private CompletableFuture<Void> generateApiSpec(String userId, String[] filenamesRelatedException, String requestId,
		File cloneDir, List<APIMetadata> apiMetaDatas) {
		if (apiMetaDatas == null) {// 이전 단계에서 실패한 경우 후속 작업 생략
			return null;
		}
		// API 문서 생성
		return CompletableFuture.runAsync(() -> {
			try {
				llmService.generateV1(userId, apiMetaDatas, cloneDir, filenamesRelatedException, requestId);
			} catch (IOException e) {
				throw new CompletionException(e);
			}
		}).exceptionally(ex -> {
			handleGenerateApiSpecUnknownException(requestId,userId,apiMetaDatas,ex);
			return null;
		});
	}

	private CompletableFuture<List<APIMetadata>> getApiEndpoint(String userId, ProjectMetaData metaData,
		String defaultBranchName, String requestId, ControllerFile controllerFile) {
		return CompletableFuture
			.supplyAsync(() -> { // API 엔드포인트 수집
				return apiEndpointCollectorPortInPipline.getApiEndpoints(userId, metaData,
					defaultBranchName, controllerFile, requestId);
			})
			.exceptionally(ex -> { // 실패 했을 경우 작업 컨텍스트 저장 후 null 반환
				handleEndpointCollectorException(userId,metaData,controllerFile,ex);
				return null;
			});
	}

	private void handleGenerateApiSpecUnknownException(String requestId, String userId, List<APIMetadata> apiMetaDatas, Throwable ex) {
		//필요한 컨텍스트 저장 후, 이후 사용자 요청시 재시도
	}

	private void handleEndpointCollectorException(String userId, ProjectMetaData metaData,
		ControllerFile controllerFile, Throwable ex) {
		//필요한 컨텍스트 저장 후, 이후 사용자 요청시 재시도
	}

	private Void handleFinalJoinException(Throwable ex) {
		return null;
	}
}