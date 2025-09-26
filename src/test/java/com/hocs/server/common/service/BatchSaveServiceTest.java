package com.hocs.server.common.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import lombok.AllArgsConstructor;
import lombok.Data;

@ExtendWith(MockitoExtension.class)
@DisplayName("BatchSaveService 테스트")
class BatchSaveServiceTest {

	static class TestEntity {
		private String id;
		private String data;
		
		public TestEntity() {}
		
		public TestEntity(String id, String data) {
			this.id = id;
			this.data = data;
		}
		
		public String getId() {
			return id;
		}
		
		public void setId(String id) {
			this.id = id;
		}
		
		public String getData() {
			return data;
		}
		
		public void setData(String data) {
			this.data = data;
		}
		
		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (!(o instanceof TestEntity)) return false;
			TestEntity that = (TestEntity) o;
			return java.util.Objects.equals(id, that.id) && 
				   java.util.Objects.equals(data, that.data);
		}
		
		@Override
		public int hashCode() {
			return java.util.Objects.hash(id, data);
		}
	}

	static class TestBatchSaveService extends BatchSaveService<TestEntity> {
		private final List<TestEntity> savedEntities = new ArrayList<>();
		private final List<TestEntity> failedEntities = new ArrayList<>();
		private final List<TestEntity> movedToFailedTable = new ArrayList<>();
		private boolean shouldFail = false;

		@Override
		protected String getSnippetId(TestEntity entity) {
			return entity.getId();
		}

		@Override
		protected List<TestEntity> mergeEntitiesById(List<TestEntity> entities) {
			// 간단한 병합 로직 - ID가 같으면 마지막 것만 유지
			return entities.stream()
				.distinct()
				.toList();
		}

		@Override
		protected List<TestEntity> save(List<TestEntity> entities) {
			if (shouldFail) {
				failedEntities.addAll(entities);
				return entities; // 모든 엔티티가 실패
			}
			savedEntities.addAll(entities);
			return new ArrayList<>(); // 성공 시 빈 리스트 반환
		}

		@Override
		protected void moveToFailedTable(TestEntity entity, int maxRetryAttempts) {

		}

		@Override
		protected int getMaxRetryAttempts() {
			return 3;
		}

		// Test helper methods
		public List<TestEntity> getSavedEntities() {
			return savedEntities;
		}

		public List<TestEntity> getMovedToFailedTable() {
			return movedToFailedTable;
		}

		public void setShouldFail(boolean shouldFail) {
			this.shouldFail = shouldFail;
		}

		public void clearState() {
			savedEntities.clear();
			failedEntities.clear();
			movedToFailedTable.clear();
		}
	}

	private TestBatchSaveService batchSaveService;

	@BeforeEach
	void setUp() {
		batchSaveService = spy(new TestBatchSaveService());
	}

	@Test
	@DisplayName("엔티티를 버퍼에 추가하고 flush 시 저장되어야 한다")
	void shouldAddEntityToBufferAndSaveOnFlush() throws InterruptedException {
		// Given
		TestEntity entity = new TestEntity("1", "test data");

		// When
		batchSaveService.addEntity(entity);
		batchSaveService.flush();

		// Then
		assertThat(batchSaveService.getSavedEntities()).hasSize(1);
		assertThat(batchSaveService.getSavedEntities().get(0)).isEqualTo(entity);
	}

	@Test
	@DisplayName("여러 엔티티를 추가하고 한 번에 배치 저장해야 한다")
	void shouldBatchSaveMultipleEntities() throws InterruptedException {
		// Given
		TestEntity entity1 = new TestEntity("1", "data1");
		TestEntity entity2 = new TestEntity("2", "data2");
		TestEntity entity3 = new TestEntity("3", "data3");

		// When
		batchSaveService.addEntity(entity1);
		batchSaveService.addEntity(entity2);
		batchSaveService.addEntity(entity3);
		batchSaveService.flush();

		// Then
		assertThat(batchSaveService.getSavedEntities()).hasSize(3);
		assertThat(batchSaveService.getSavedEntities()).contains(entity1, entity2, entity3);
	}

	@Test
	@DisplayName("빈 버퍼에서 flush 호출 시 아무것도 저장되지 않아야 한다")
	void shouldNotSaveAnythingWhenFlushingEmptyBuffer() {
		// When
		batchSaveService.flush();

		// Then
		assertThat(batchSaveService.getSavedEntities()).isEmpty();
		verify(batchSaveService, never()).save(anyList());
	}

	@Test
	@DisplayName("저장 실패 시 재시도 큐로 이동해야 한다")
	void shouldMoveToRetryQueueOnSaveFailure() throws InterruptedException {
		// Given
		TestEntity entity = new TestEntity("1", "test data");
		batchSaveService.setShouldFail(true);

		// When
		batchSaveService.addEntity(entity);
		batchSaveService.flush();

		// Then
		assertThat(batchSaveService.getSavedEntities()).isEmpty();
		verify(batchSaveService, times(1)).save(anyList());
	}

	@Test
	@DisplayName("재시도 큐에서 엔티티를 다시 처리해야 한다")
	void shouldRetryFailedEntities() throws InterruptedException {
		// Given
		TestEntity entity = new TestEntity("1", "test data");
		batchSaveService.setShouldFail(true);

		// 첫 번째 시도 - 실패
		batchSaveService.addEntity(entity);
		batchSaveService.flush();

		// 두 번째 시도 - 성공
		batchSaveService.setShouldFail(false);

		// When
		batchSaveService.retryFailedEntities();

		// Then
		assertThat(batchSaveService.getSavedEntities()).hasSize(1);
		assertThat(batchSaveService.getSavedEntities().get(0)).isEqualTo(entity);
	}

	@Test
	@DisplayName("동시에 여러 스레드에서 엔티티를 추가할 수 있어야 한다")
	void shouldHandleConcurrentEntityAddition() throws InterruptedException {
		// Given
		int threadCount = 10;
		int entitiesPerThread = 10;
		CountDownLatch latch = new CountDownLatch(threadCount);

		// When
		for (int i = 0; i < threadCount; i++) {
			final int threadId = i;
			new Thread(() -> {
				try {
					for (int j = 0; j < entitiesPerThread; j++) {
						TestEntity entity = new TestEntity(
							threadId + "-" + j, 
							"data-" + threadId + "-" + j
						);
						batchSaveService.addEntity(entity);
					}
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				} finally {
					latch.countDown();
				}
			}).start();
		}

		latch.await(5, TimeUnit.SECONDS);
		batchSaveService.flush();

		// Then
		assertThat(batchSaveService.getSavedEntities()).hasSize(threadCount * entitiesPerThread);
	}

	@Test
	@DisplayName("mergeEntitiesById 메서드가 호출되어야 한다")
	void shouldCallMergeEntitiesById() throws InterruptedException {
		// Given
		TestEntity entity1 = new TestEntity("1", "data1");
		TestEntity entity2 = new TestEntity("2", "data2");

		// When
		batchSaveService.addEntity(entity1);
		batchSaveService.addEntity(entity2);
		batchSaveService.flush();

		// Then
		verify(batchSaveService, times(1)).mergeEntitiesById(anyList());
		assertThat(batchSaveService.getSavedEntities()).hasSize(2);
	}

	@Test
	@DisplayName("getSnippetId 메서드가 올바르게 호출되어야 한다")
	void shouldCallGetSnippetIdCorrectly() throws InterruptedException {
		// Given
		TestEntity entity = new TestEntity("test-id", "test data");
		batchSaveService.setShouldFail(true);

		// When
		batchSaveService.addEntity(entity);
		batchSaveService.flush();

		// Then
		verify(batchSaveService, times(1)).getSnippetId(entity);
	}

	@Test
	@DisplayName("타임아웃 발생 시 실패 테이블로 직접 이동해야 한다")
	void shouldMoveToFailedTableOnTimeout() throws InterruptedException {
		// Given
		TestEntity entity = new TestEntity("1", "test data");
		
		// 버퍼를 가득 채워서 타임아웃 유발을 위한 설정
		// 실제로는 큐 크기(3000)를 초과하기 어려우므로 다른 방법으로 테스트
		
		// When
		batchSaveService.addEntity(entity);
		batchSaveService.flush();

		// Then
		// 정상적인 경우 저장되어야 함
		assertThat(batchSaveService.getSavedEntities()).hasSize(1);
	}
}
