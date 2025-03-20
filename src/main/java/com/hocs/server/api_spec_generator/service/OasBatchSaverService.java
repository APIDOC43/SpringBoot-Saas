package com.hocs.server.api_spec_generator.service;

import com.hocs.server.api_spec_generator.domain.output.OAS;
import com.hocs.server.api_spec_generator.domain.output.PathItem;
import com.hocs.server.api_spec_generator.domain.output.Schema;
import com.hocs.server.api_spec_generator.repository.OasRepositoryCustom;
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
import org.springframework.transaction.annotation.Transactional;

@Service
public class OasBatchSaverService {
	private final BlockingQueue<OAS> entityBuffer = new ArrayBlockingQueue<>(3000,true);
	// 재시도 큐
	private final BlockingQueue<OAS> retryQueue = new ArrayBlockingQueue<>(3000,true);
	// 재시도 횟수 관리 (동일한 OAS ID 별 최대 재시도 횟수 제한)
	private final Map<String, Integer> retryCountMap = new ConcurrentHashMap<>();
	private static final int MAX_RETRY = 3;
	private static final int MAX_DRAIN_VALUE = 1000;

	@Autowired
	private OasRepositoryCustom repository;

	// 엔티티를 버퍼에 추가 (Producer)
	public void addEntity(OAS entity) {
		try {
			entityBuffer.put(entity);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	// 버퍼에 쌓인 엔티티들을 주기적으로 배치 저장 (Consumer)
	@Transactional
	@Scheduled(fixedDelay = 30000)
	public void flushEntities() {
		List<OAS> entitiesToSave = new ArrayList<>();
		// drainTo()를 사용하여 버퍼에 있는 최대 MAX_DRAIN_VALUE 개의 엔티티를 가져옴
		entityBuffer.drainTo(entitiesToSave, MAX_DRAIN_VALUE);
		if (!entitiesToSave.isEmpty()) {
			List<OAS> mergedEntities = mergeEntitiesById(entitiesToSave);
			int successCount = repository.bulkWrite(mergedEntities);
			if (successCount != mergedEntities.size()) {
				handleFailedEntities(mergedEntities, successCount);
			}
		}
	}

	// 재시도 큐에 쌓인 엔티티들을 주기적으로 처리 (재시도)
	@Transactional
	@Scheduled(fixedDelay = 300000) // 5분마다 재시도
	public void flushRetryEntities() {
		List<OAS> retryEntities = new ArrayList<>();
		retryQueue.drainTo(retryEntities, MAX_DRAIN_VALUE);
		if (!retryEntities.isEmpty()) {
			List<OAS> mergedEntities = mergeEntitiesById(retryEntities);
			int successCount = repository.bulkWrite(mergedEntities);
			if (successCount != mergedEntities.size()) {
				handleFailedEntities(mergedEntities, successCount);
			}
		}
	}

	// 실패한 엔티티 처리 (재시도 또는 실패 테이블로 이동)
	private void handleFailedEntities(List<OAS> mergedEntities, int successCount) {
		System.err.println("⚠️ 일부 OAS 저장 실패! 성공: " + successCount + " / 총 요청: " + mergedEntities.size());
		// successCount 이후의 엔티티들을 실패한 것으로 간주
		List<OAS> failedEntities = mergedEntities.subList(successCount, mergedEntities.size());
		for (OAS failedEntity : failedEntities) {
			String entityId = failedEntity.getId();
			int currentRetry = retryCountMap.getOrDefault(entityId, 0);
			if (currentRetry < MAX_RETRY) {
				retryCountMap.put(entityId, currentRetry + 1);
				try {
					retryQueue.put(failedEntity);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			} else {
				moveToFailedTable(failedEntity);
			}
		}
	}

	// 최대 재시도 초과한 엔티티 처리
	private void moveToFailedTable(OAS failedEntity) {
		System.err.println("최대 재시도 초과! 실패한 OAS를 DB에 저장: " + failedEntity.getId());
		// 실패 관리 DB에 저장하거나 알림 전송 로직 추가 가능
		// 예: failedOasRepository.save(failedEntity);
	}

	// 동일한 ID를 가진 엔티티 병합
	private List<OAS> mergeEntitiesById(List<OAS> entities) {
		Map<String, OAS> mergedMap = new HashMap<>();
		for (OAS oas : entities) {
			String id = oas.getId();
			if (mergedMap.containsKey(id)) {
				OAS existing = mergedMap.get(id);
				// pathList 병합
				Map<String, List<Map<String, PathItem>>> mergedPathList = mergePathList(existing.getPathList(), oas.getPathList());
				// schemasMap 병합
				Map<String, List<Schema>> mergedSchemasMap = mergeSchemasMap(existing.getSchemasMap(), oas.getSchemasMap());
				// 병합된 OAS 객체 생성
				OAS mergedOas = OAS.create(id, existing.getInfo(), mergedPathList, mergedSchemasMap);
				mergedMap.put(id, mergedOas);
			} else {
				mergedMap.put(id, oas);
			}
		}
		return new ArrayList<>(mergedMap.values());
	}

	private Map<String, List<Map<String, PathItem>>> mergePathList(
		Map<String, List<Map<String, PathItem>>> a,
		Map<String, List<Map<String, PathItem>>> b) {
		if (a == null) return b;
		if (b == null) return a;
		Map<String, List<Map<String, PathItem>>> merged = new HashMap<>(a);
		for (Map.Entry<String, List<Map<String, PathItem>>> entry : b.entrySet()) {
			String key = entry.getKey();
			List<Map<String, PathItem>> list = entry.getValue();
			merged.merge(key, list, (existingList, newList) -> {
				List<Map<String, PathItem>> combined = new ArrayList<>(existingList);
				combined.addAll(newList);
				return combined;
			});
		}
		return merged;
	}

	private Map<String, List<Schema>> mergeSchemasMap(
		Map<String, List<Schema>> a,
		Map<String, List<Schema>> b) {
		if (a == null) return b;
		if (b == null) return a;
		Map<String, List<Schema>> merged = new HashMap<>(a);
		for (Map.Entry<String, List<Schema>> entry : b.entrySet()) {
			String key = entry.getKey();
			List<Schema> list = entry.getValue();
			merged.merge(key, list, (existingList, newList) -> {
				List<Schema> combined = new ArrayList<>(existingList);
				combined.addAll(newList);
				return combined;
			});
		}
		return merged;
	}
}
