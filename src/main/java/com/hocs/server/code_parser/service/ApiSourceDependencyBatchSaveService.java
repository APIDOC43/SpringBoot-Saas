package com.hocs.server.code_parser.service;

import com.hocs.server.code_parser.core.domain.API;
import com.hocs.server.code_parser.core.domain.APISourceDependencyInfo;
import com.hocs.server.code_parser.repository.APISourceDependencyRepositoryCustomImpl;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class ApiSourceDependencyBatchSaveService {

	private final BlockingQueue<APISourceDependencyInfo> entityBuffer = new ArrayBlockingQueue<>(1000);
	private final BlockingQueue<APISourceDependencyInfo> retryQueue = new LinkedBlockingQueue<>();
	private final Map<String, Integer> retryCountMap = new ConcurrentHashMap<>();
	private static final int MAX_RETRY = 3;

	@Autowired
	private APISourceDependencyRepositoryCustomImpl repository;


	/**
	 * 메인 큐에 엔티티 추가 (Producer)
	 */
	public void addEntity(APISourceDependencyInfo entity) {
		try {
			entityBuffer.put(entity);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	/**
	 * 메인 큐의 엔티티들을 배치 저장 (Consumer)
	 */
	@Scheduled(fixedDelay = 30000)
	public void flushEntities() {
		List<APISourceDependencyInfo> entitiesToSave = new ArrayList<>();
		entityBuffer.drainTo(entitiesToSave);

		if (!entitiesToSave.isEmpty()) {
			List<APISourceDependencyInfo> mergedEntities = mergeEntitiesById(entitiesToSave);
			int successCount = repository.bulkWrite(mergedEntities);

			if (successCount != mergedEntities.size()) {
				handleFailedEntities(mergedEntities, successCount);
			}
		}
	}

	/**
	 * 실패한 엔티티 처리 (재시도 또는 DB 저장)
	 */
	private void handleFailedEntities(List<APISourceDependencyInfo> mergedEntities, int successCount) {
		System.err.println("⚠️ 일부 엔티티 저장 실패! 성공: " + successCount + " / 총 요청: " + mergedEntities.size());

		List<APISourceDependencyInfo> failedEntities = mergedEntities.subList(successCount, mergedEntities.size());

		for (APISourceDependencyInfo failedEntity : failedEntities) {
			String entityId = failedEntity.getId();
			int currentRetry = retryCountMap.getOrDefault(entityId, 0);

			if (currentRetry < MAX_RETRY) {
				retryCountMap.put(entityId, currentRetry + 1);
				try {
					retryQueue.put(failedEntity);  // 재시도 큐에 삽입
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			} else {
				moveToFailedTable(failedEntity);  // 실패 관리 테이블로 이동
			}
		}
	}

	/**
	 * 실패한 엔티티를 별도 DB에 저장하고 알림 전송
	 */
	private void moveToFailedTable(APISourceDependencyInfo failedEntity) {
		System.err.println("최대 재시도 초과! 실패한 엔티티를 DB에 저장: " + failedEntity.getId());
//		failedDependencyRepository.save(failedEntity);  // 실패 관리 DB에 저장
//		notificationService.sendFailureAlert(failedEntity);  // 알림 전송 (Slack, 이메일 등)
	}

	/**
	 * 재시도 큐에서 주기적으로 엔티티 처리
	 */
	@Scheduled(fixedDelay = 300000)  // 5분마다 재시도 실행
	public void retryFailedEntities() {
		List<APISourceDependencyInfo> retryEntities = new ArrayList<>();
		retryQueue.drainTo(retryEntities);

		if (!retryEntities.isEmpty()) {
			int successCount = repository.bulkWrite(retryEntities);

			if (successCount != retryEntities.size()) {
				handleFailedEntities(retryEntities, successCount);
			}
		}
	}

	/**
	 * 동일한 ID를 가진 엔티티를 병합
	 */
	private List<APISourceDependencyInfo> mergeEntitiesById(List<APISourceDependencyInfo> entitiesToSave) {
		Map<String, APISourceDependencyInfo> mergedMap = new HashMap<>();

		for (APISourceDependencyInfo entity : entitiesToSave) {
			String id = entity.getId();

			if (mergedMap.containsKey(id)) {
				APISourceDependencyInfo existing = mergedMap.get(id);
				List<API> existingApis = existing.getApiSourceDependencies();
				if (entity.getApiSourceDependencies() != null) {
					existingApis.addAll(entity.getApiSourceDependencies());
				}
			} else {
				List<API> apiList = new ArrayList<>();
				if (entity.getApiSourceDependencies() != null) {
					apiList.addAll(entity.getApiSourceDependencies());
				}
				APISourceDependencyInfo newEntity = APISourceDependencyInfo.create(
					entity.getId(),
					entity.getUserId(),
					apiList,
					entity.getGlobal()
				);
				mergedMap.put(id, newEntity);
			}
		}
		return new ArrayList<>(mergedMap.values());
	}
}