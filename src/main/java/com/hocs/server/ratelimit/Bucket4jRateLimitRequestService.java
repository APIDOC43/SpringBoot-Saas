package com.hocs.server.ratelimit;


import com.hocs.server.pipline.request.RateLimitRequestDataImpl;
import com.hocs.server.pipline.service.ApiDocPiplineService;
import io.github.bucket4j.Bucket;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class Bucket4jRateLimitRequestService {

	// Bucket4j 토큰 버킷: 초기 토큰 20개, 자동 재공급은 없음 (refill rate = 0)
	private final Bucket bucket;
	private final RequestQueueService requestQueueService;
	private final ApiDocPiplineService pipelineService;


	/**
	 * 새로운 요청을 처리합니다.
	 * - Bucket에서 토큰을 바로 소비할 수 있으면 파이프라인 실행 후 작업 완료 시 토큰을 공급합니다.
	 * - 토큰이 부족하면 요청을 내부 큐에 추가합니다.
	 */
	public void handleNewRequest(RateLimitRequest request) {
		System.out.println("새로운 요청이 들어왔습니다.");
		if (bucket.tryConsume(1)) {
			System.out.println("토큰 소비됨, 파이프라인 실행");
			RateLimitRequestDataImpl data = (RateLimitRequestDataImpl) request.getData();

			// 파이프라인 처리 시작 및 완료 후 토큰 공급
			pipelineProcess(data);


		} else {
			System.out.println("토큰 부족, 요청 큐에 추가");
			requestQueueService.addRequest(request);
		}
	}

	/**
	 * 대기 중인 요청들을 처리합니다.
	 * - 큐에 요청이 있고, Bucket에서 토큰 소비가 가능하면 요청을 꺼내 처리합니다.
	 * - 처리 완료 후 토큰 공급합니다.
	 */
	public void processQueuedRequests() {
		while (!requestQueueService.isEmpty() && bucket.tryConsume(1)) {
			RateLimitRequest request = requestQueueService.pollRequest();
			if (request != null) {
				System.out.println("대기 큐 요청 처리: 토큰 소비됨");
				RateLimitRequestDataImpl data = (RateLimitRequestDataImpl) request.getData();
				pipelineProcess(data);
			}
		}
	}

	private void pipelineProcess(RateLimitRequestDataImpl data) {
		CompletableFuture.runAsync(()-> {
			pipelineService.executeAsync(
				data.getRequest().getUserId(),
				data.getMetaData(),
				data.getFilenamesRelatedException(),
				data.getDefaultBranchName(),
				data.getExcludeApiInfo()
			);
		}).thenRun(() -> {
			bucket.addTokens(1); // 작업 완료 후 토큰 공급
			processQueuedRequests();
		}).exceptionally(ex -> {
			System.err.println("파이프라인 실행 중 오류 발생: " + ex.getMessage());
			// 오류가 발생해도 토큰을 보충하여 시스템 정합성을 유지
			bucket.addTokens(1);
			processQueuedRequests();
			return null;
		});
	}
}
