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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OasBatchSaverService {

	// BlockingQueue를 사용하여 생산자-소비자 패턴 구현 (용량은 상황에 맞게 조정)
	private final BlockingQueue<OAS> entityBuffer = new ArrayBlockingQueue<>(1000);

	@Autowired
	private OasRepositoryCustom repository;

	// 엔티티를 버퍼에 추가 (Producer)
	public void addEntity(OAS entity) {
		try {
			// 큐가 꽉 차면 put() 호출 시 대기하므로 안전하게 처리됨
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
		// drainTo()는 내부적으로 락을 사용하여 원자적으로 모든 요소를 가져오고 큐를 비움
		entityBuffer.drainTo(entitiesToSave);
		if (!entitiesToSave.isEmpty()) {
			List<OAS> mergedEntities = mergeEntitiesById(entitiesToSave);
			repository.bulkWrite(mergedEntities);
		}
	}

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
