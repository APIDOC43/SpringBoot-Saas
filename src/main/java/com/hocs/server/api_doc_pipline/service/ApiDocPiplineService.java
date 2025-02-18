package com.hocs.server.api_doc_pipline.service;

import com.hocs.server.api_doc_pipline.domain.ApiInfoInPipline;
import com.hocs.server.api_doc_pipline.domain.ControllerFile;
import com.hocs.server.api_doc_pipline.service.out.port.ApiEndpointCollectorPortInPipline;
import com.hocs.server.openai.domain.input.APIMetadata;
import com.hocs.server.openai.service.GenerateOasFacadeService;
import com.hocs.server.openai.util.HttpClient;
import com.hocs.server.common.domain.ProjectMetaData;
import com.hocs.server.common.domain.ApiInfo;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApiDocPiplineService {

	private final ApiEndpointCollectorPortInPipline apiEndpointCollectorPortInPipline;
	private final GenerateOasFacadeService llmService;

	public void execute(
		String userId,
		ProjectMetaData metaData,
		String[] filenamesRelatedException,
		String defaultBranchName,
		List<ApiInfo> excludeApiInfoInPipline) throws Exception
	{
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
				userId, metaData, defaultBranchName, controllerFile);

			//task 2
			//LLM을 이용하여 OAS 데이터를 생성합니다.
			File cloneDir = metaData.getProjectRootPath().getToFile();
			llmService.generateV1(userId, apiMetadata, cloneDir,filenamesRelatedException);

			//task 3
			//OAS데이터를 렌더링합니다.
			HttpClient.toSaas(cloneDir, userId);
		}
	}

	public void executeAsync(
		String userId, ProjectMetaData metaData,
		String[] filenamesRelatedException,
		String defaultBranchName,
		List<ApiInfo> excludeApiInfoInPipline)
	{
		//task 0;
		Map<ControllerFile, List<ApiInfoInPipline>> apiEndpointInfo = apiEndpointCollectorPortInPipline.findApiInfo(
			metaData.getCodingLanguage(),
			metaData.getProjectFramework(),
			metaData.getProjectRootPath(),
			excludeApiInfoInPipline, 100);

		int numThreads = 5;
		System.out.println("numThreads = " + numThreads);
		ExecutorService executorService = Executors.newFixedThreadPool(numThreads);
		RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
		List<CompletableFuture<Void>> futures = new ArrayList<>();

		for (ControllerFile controllerFile : apiEndpointInfo.keySet()) {
			CompletableFuture<Void> future = CompletableFuture
				// Task 1: 각 컨트롤러 파일에서 Endpoint별 API 정보를 추출합니다.
				.supplyAsync(() -> {
					RequestContextHolder.setRequestAttributes(requestAttributes);
					return apiEndpointCollectorPortInPipline.getApiEndpoints(userId, metaData, defaultBranchName, controllerFile);
				}, executorService)
				// Task 2: LLM을 이용하여 OAS 데이터를 생성합니다.
				.thenCompose(apiMetadata -> {
					File cloneDir = metaData.getProjectRootPath().getToFile();
					return CompletableFuture.runAsync(() -> {
							try {
								llmService.generateV1(userId, apiMetadata, cloneDir, filenamesRelatedException);
							} catch (IOException e) {
								throw new CompletionException(e);
							}
						}, executorService)
						// Task 2 실행 후 cloneDir를 다음 체이닝으로 전달합니다.
						.thenApply(voidResult -> cloneDir);
				})
				// Task 3: OAS 데이터를 렌더링합니다.
				.thenAccept(cloneDir -> {
					HttpClient.toSaas(cloneDir, userId);
				})
				// 체이닝 작업 완료 후 RequestContext 정리
				.whenComplete((result, throwable) -> {
					RequestContextHolder.resetRequestAttributes();
				});
			futures.add(future);
		}
		// 모든 비동기 작업이 완료될 때까지 대기합니다.
		CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
		// 작업 완료 후 스레드풀을 종료합니다.
		executorService.shutdown();
	}
}