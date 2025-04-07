package com.hocs.server.api_spec_generator.service;

import com.hocs.server.api_spec_generator.domain.output.OAS;
import com.hocs.server.api_spec_generator.domain.output.PathItem;
import com.hocs.server.api_spec_generator.domain.output.Schema;
import com.hocs.server.api_spec_generator.repository.OasRepositoryCustom;
import com.hocs.server.common.service.BatchSaveService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class OasBatchSaverService extends BatchSaveService<OAS>{
	private static final long FLUSH_DELAY_MS = 30000;
	private static final long RETRY_DELAY_MS = 30000;

	private final OasRepositoryCustom repository;

	@Override
	public void addEntity(OAS entity) throws InterruptedException {
		super.addEntity(entity);
	}

	@Scheduled(fixedDelay = FLUSH_DELAY_MS)
	public void flushEntities() {
		super.flush();
	}

	@Scheduled(fixedDelay = RETRY_DELAY_MS)
	public void flushRetryEntities() {
		super.retryFailedEntities();
	}

	@Override
	protected String getSnippetId(OAS entity) {
		return entity.getSnippetId();
	}

	@Override
	protected void moveToFailedTable(OAS failedEntity) {
		// TODO: 실패 관리 DB에 저장하거나 알림 전송 로직 추가
		System.err.println("최대 재시도 초과! 실패한 엔티티를 DB에 저장: " + failedEntity.getId());
//		failedDependencyRepository.save(failedEntity);  // TODO:실패 관리 DB에 저장
//		notificationService.sendFailureAlert(failedEntity);  // 알림 전송 (Slack, 이메일 등)
	}

	@Override
	protected List<OAS> save(List<OAS> entities) {
		return repository.bulkWrite(entities);
	}

	/**
	 * 동일 ID를 가진 엔티티 병합
	 */
	protected List<OAS> mergeEntitiesById(List<OAS> entities) {
		Map<String, OAS> mergedMap = new HashMap<>();

		for (OAS oas : entities) {
			String id = oas.getId();

			if (mergedMap.containsKey(id)) {
				OAS existing = mergedMap.get(id);
				Map<String, List<Map<String, PathItem>>> mergedPathList = mergePathList(existing.getPathList(), oas.getPathList());
				Map<String, List<Schema>> mergedSchemasMap = mergeSchemasMap(existing.getSchemasMap(), oas.getSchemasMap());
				OAS mergedOas = OAS.create(id, id, existing.getInfo(), mergedPathList, mergedSchemasMap);
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