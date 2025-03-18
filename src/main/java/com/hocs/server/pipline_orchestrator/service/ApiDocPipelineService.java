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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApiDocPipelineService {

	private final ApiEndpointCollectorPortInPipline apiEndpointCollectorPortInPipline;
	private final GenerateOasFacadeService llmService;
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
		ExecutorService executorService = Executors.newFixedThreadPool(4);
		List<CompletableFuture<Void>> futures = new ArrayList<>();

		for (ControllerFile controllerFile : apiEndpointInfo.keySet()) {
			CompletableFuture<Void> future = CompletableFuture
				.supplyAsync(() -> {
					// API 엔드포인트 수집
					return apiEndpointCollectorPortInPipline.getApiEndpoints(userId, metaData,
						defaultBranchName, controllerFile, requestId);
				}, executorService)
				.handle((result, ex) -> {
					if (ex != null) {
						log.error("Error occurred in async [getApiEndpoints] for controller file: "
							+ controllerFile.getClassName(), ex);
						// 예외 발생 시 별도의 후속 처리
					}
					return result;
				})
				.thenAccept(apiMetadata -> {
					// API 문서 생성
					try {
						llmService.generateV1(userId, apiMetadata, cloneDir,
							filenamesRelatedException, requestId);
					} catch (IOException e) {
						throw new CompletionException(e);
					}
				})
				// handle를 사용해 예외와 정상 결과를 모두 처리
				.handle((result, ex) -> {
					if (ex != null) {
						log.error("Error occurred in generate_OAS task for controller file: "
							+ controllerFile.getClassName(), ex);
						// 예외 발생 시 별도의 후속 처리
					}
					return null;
				});
			futures.add(future);
		}

		// 모든 비동기 작업이 완료될 때까지 대기합니다.
		CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
		executorService.shutdownNow();
		// Task 3: OAS 데이터를 렌더링합니다.
		oasSendClient.toSaas(cloneDir, userId);
	}
}