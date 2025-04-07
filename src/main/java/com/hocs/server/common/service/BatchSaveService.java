package com.hocs.server.common.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public abstract class BatchSaveService<T> {

	private final BlockingQueue<T> entityBuffer = new ArrayBlockingQueue<>(3000, true);
	private final BlockingQueue<T> retryQueue = new ArrayBlockingQueue<>(3000, true);
	private final Map<String, Integer> retryCountMap = new ConcurrentHashMap<>();

	private static final int MAX_RETRY = 3;
	private static final int MAX_DRAIN_VALUE = 1000;
	private static final long OFFER_TIMEOUT_SEC = 10;
	/**
	 * 메인 큐에 엔티티 추가 (Producer)*
	 * 현재는 동일 ID의 중복 요청이 addEntity()로 들어오지 않는다고 보장됨.
	 * 추후 구조가 변경되어 중복 입력 가능성이 생긴다면,
	 * retryQueue에 중복 삽입 방지를 위한 별도 Set 또는 Map 도입 필요.
	 */
	public void addEntity(T entity) throws InterruptedException {
		boolean offered = entityBuffer.offer(entity, OFFER_TIMEOUT_SEC, TimeUnit.SECONDS);
		if (!offered) { //timeout
			handleFailedEntities(List.of(entity));
		}
	}

	/**
	 * 메인 큐의 엔티티들을 배치 저장 (Consumer)
	 */
	public void flush() {
		List<T> entitiesToSave = new ArrayList<>();
		entityBuffer.drainTo(entitiesToSave, MAX_DRAIN_VALUE);

		if (!entitiesToSave.isEmpty()) {
			List<T> mergedEntities = mergeEntitiesById(entitiesToSave);
			List<T> failedEntities = save(mergedEntities);
			handleFailedEntities(failedEntities);
		}
	}
	/**
	 * 재시도 큐에서 주기적으로 엔티티 처리
	 */
	public void retryFailedEntities() {
		List<T> retryEntities = new ArrayList<>();
		retryQueue.drainTo(retryEntities, MAX_DRAIN_VALUE);

		if (!retryEntities.isEmpty()) {
			List<T> failedEntities = save(retryEntities);
			handleFailedEntities(failedEntities);
		}
	}

	/**
	 * 실패한 엔티티 처리 (재시도 또는 DB 저장)
	 */
	private void handleFailedEntities(List<T> failedEntities) {
		for (T entity : failedEntities) {
			String id = getId(entity);
			int retryCount = retryCountMap.getOrDefault(id, 0);

			if (retryCount < MAX_RETRY) {
				retryCountMap.put(id, retryCount + 1);
				try {
					boolean offered = retryQueue.offer(entity, OFFER_TIMEOUT_SEC, TimeUnit.SECONDS);
					if (!offered) { //timeout
						moveToFailedTable(entity);
						retryCountMap.remove(id);
					}
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			} else {
				retryCountMap.remove(id);
				moveToFailedTable(entity);
			}
		}
	}

	protected abstract String getId(T entity);

	protected abstract List<T> mergeEntitiesById(List<T> entities);

	protected abstract List<T> save(List<T> entities);

	protected abstract void moveToFailedTable(T entity);
}
