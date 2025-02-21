package com.hocs.server.custom_rag.service;

import com.hocs.server.custom_rag.legacy.extractor.domain.API;
import com.hocs.server.custom_rag.legacy.extractor.domain.APISourceDependencyInfo;
import com.hocs.server.custom_rag.legacy.extractor.respository.mongo.APISourceDependencyRepositoryCustomImpl;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ApiSourceDependencyBatchSaveService {

	private final List<APISourceDependencyInfo> entityBuffer = Collections.synchronizedList(new ArrayList<>());
	private static final ReentrantLock lock = new ReentrantLock();

	@Autowired
	private APISourceDependencyRepositoryCustomImpl repository;

	// 엔티티를 버퍼에 추가
	public void addEntity(APISourceDependencyInfo entity) {
		lock.lock();
		try {
			entityBuffer.add(entity);
		}finally {
			lock.unlock();
		}

	}

	@Transactional
	@Scheduled(fixedDelay = 30000)
	public void flushEntities() {
		lock.lock();
		try {
			if (!entityBuffer.isEmpty()) {
				List<APISourceDependencyInfo> entitiesToSave = new ArrayList<>(entityBuffer);
				entityBuffer.clear();
				// 동일 id 기준으로 병합
				List<APISourceDependencyInfo> mergedEntities = mergeEntitiesById(entitiesToSave);
				repository.bulkWrite(mergedEntities);
			}
		} finally {
			lock.unlock();
		}
	}

	private List<APISourceDependencyInfo> mergeEntitiesById(List<APISourceDependencyInfo> entitiesToSave) {
		// id를 키로 하는 병합된 엔티티를 저장할 Map 생성
		Map<String, APISourceDependencyInfo> mergedMap = new HashMap<>();

		for (APISourceDependencyInfo entity : entitiesToSave) {
			String id = entity.getId();

			// 기존에 동일 id가 있다면 API 목록을 병합
			if (mergedMap.containsKey(id)) {
				APISourceDependencyInfo existing = mergedMap.get(id);

				// 기존 객체의 API 목록에 새 객체의 API 목록을 추가 (null 체크)
				List<API> existingApis = existing.getApiSourceDependencies();
				if (entity.getApiSourceDependencies() != null) {
					existingApis.addAll(entity.getApiSourceDependencies());
				}
			} else {
				// 새 객체라면, API 목록을 새로운 ArrayList로 복사하여 사용 (이후 병합이 가능하도록)
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