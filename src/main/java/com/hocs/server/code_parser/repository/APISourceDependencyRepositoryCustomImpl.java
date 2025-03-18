package com.hocs.server.code_parser.repository;

import com.hocs.server.code_parser.core.domain.APISourceDependencyInfo;
import com.mongodb.bulk.BulkWriteResult;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

@Repository
public class APISourceDependencyRepositoryCustomImpl implements APISourceDependencyRepositoryCustom {

	@Autowired
	private MongoTemplate mongoTemplate;

	@Override
	public int bulkWrite(List<APISourceDependencyInfo> mergedEntities) {
		// UNORDERED 모드로 BulkOperations 생성
		BulkOperations bulkOps = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, APISourceDependencyInfo.class);

		for (APISourceDependencyInfo entity : mergedEntities) {
			// _id 필드를 기준으로 조건 작성 (MongoDB는 기본적으로 _id 필드를 사용)
			Query query = new Query(Criteria.where("_id").is(entity.getId()));
			Update update = new Update();

			// apiSourceDependencies 필드에 새로운 API들을 추가
			if (entity.getApiSourceDependencies() != null && !entity.getApiSourceDependencies().isEmpty()) {
				update.addToSet("apiSourceDependencies").each(entity.getApiSourceDependencies().toArray());
			}

			// 다른 필드들도 업데이트
			if (entity.getUserId() != null) {
				update.set("userId", entity.getUserId());
			}
			if (entity.getGlobal() != null) {
				update.set("global", entity.getGlobal());
			}

			// upsert: 조건에 맞는 문서가 없으면 새로 생성
			bulkOps.upsert(query, update);
		}
		// 모든 bulk 연산 실행
		if (!mergedEntities.isEmpty()) {
			BulkWriteResult result = bulkOps.execute();
			return result.getModifiedCount() + result.getUpserts().size();
		}
		return 0;
	}
}

