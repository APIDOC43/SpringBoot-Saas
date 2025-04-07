package com.hocs.server.code_parser.service;

import com.hocs.server.code_parser.core.domain.API;
import com.hocs.server.code_parser.core.domain.APISourceDependencyInfo;
import com.hocs.server.code_parser.repository.APISourceDependencyRepositoryCustomImpl;
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
public class ApiSourceDependencyBatchSaveService  extends  BatchSaveService<APISourceDependencyInfo>{

	private static final long FLUSH_DELAY_MS = 30000;
	private static final long RETRY_DELAY_MS = 300000;
	private final APISourceDependencyRepositoryCustomImpl repository;

	@Scheduled(fixedDelay = FLUSH_DELAY_MS)
	@Override
	public void flush() {
		super.flush();
	}

	@Scheduled(fixedDelay = RETRY_DELAY_MS)
	@Override
	public void retryFailedEntities() {
		super.retryFailedEntities();
	}

	@Override
	public void addEntity(APISourceDependencyInfo entity) throws InterruptedException {
		super.addEntity(entity);
	}

	/**
	 * 실패한 엔티티를 별도 DB에 저장하고 알림 전송
	 */
	@Override
	protected void moveToFailedTable(APISourceDependencyInfo failedEntity) {
		System.err.println("최대 재시도 초과! 실패한 엔티티를 DB에 저장: " + failedEntity.getId());
//		failedDependencyRepository.save(failedEntity);  // TODO:실패 관리 DB에 저장
//		notificationService.sendFailureAlert(failedEntity);  // 알림 전송 (Slack, 이메일 등)
	}


	@Override
	protected String getId(APISourceDependencyInfo entity) {
		return entity.getId();
	}

	@Override
	protected List<APISourceDependencyInfo> save(List<APISourceDependencyInfo> entities) {
		return repository.bulkWrite(entities);
	}

	/**
	 * 동일한 ID를 가진 엔티티를 병합
	 */
	protected List<APISourceDependencyInfo> mergeEntitiesById(
		List<APISourceDependencyInfo> entitiesToSave) {
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