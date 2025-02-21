package com.hocs.server.pipline.service;

import com.hocs.server.pipline.domain.ApiInfoInPipline;
import com.hocs.server.pipline.domain.ControllerFile;
import com.hocs.server.pipline.service.out.port.ApiEndpointCollectorPortInPipline;
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
import java.util.UUID;
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
			HttpClient.toSaas(cloneDir, userId);
		}
	}

	public void executeAsync(
		String userId, ProjectMetaData metaData,
		String[] filenamesRelatedException,
		String defaultBranchName,
		List<ApiInfo> excludeApiInfoInPipline)
	{
		//레포정보기반으로 requestID전달 받도록 수정해야됨. 임시적을로 여기서 생성
		String requestId = UUID.randomUUID().toString();

		//task 0;
		Map<ControllerFile, List<ApiInfoInPipline>> apiEndpointInfo = apiEndpointCollectorPortInPipline.findApiInfo(
			metaData.getCodingLanguage(),
			metaData.getProjectFramework(),
			metaData.getProjectRootPath(),
			excludeApiInfoInPipline, 100);


		File cloneDir = metaData.getProjectRootPath().getToFile();
		try (ExecutorService executorService = Executors.newFixedThreadPool(5)) {
			RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
			List<CompletableFuture<Void>> futures = new ArrayList<>();

			for (ControllerFile controllerFile : apiEndpointInfo.keySet()) {
				CompletableFuture<Void> future = CompletableFuture
					.supplyAsync(() -> {
						try{
						RequestContextHolder.setRequestAttributes(requestAttributes);
						return apiEndpointCollectorPortInPipline.getApiEndpoints(
							userId, metaData, defaultBranchName, controllerFile, requestId);
						}finally {
							RequestContextHolder.resetRequestAttributes();
						}
					}, executorService)
					.thenAccept(apiMetadata -> {
						try {
							llmService.generateV1(userId, apiMetadata, cloneDir, filenamesRelatedException, requestId);
						} catch (IOException e) {
							throw new CompletionException(e);
						}
					})
					.whenComplete((result, throwable) -> {
						if (throwable != null) {
							log.error("Error occurred in async task", throwable);
						}
					});
				futures.add(future);
			}
			// 모든 비동기 작업이 완료될 때까지 대기합니다.
			CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
			executorService.shutdownNow();
		}
		// Task 3: OAS 데이터를 렌더링합니다.
		HttpClient.toSaas(cloneDir, userId);

	}
}