package com.hocs.server.api_spec_generator.repository;

import com.hocs.server.api_spec_generator.domain.output.OAS;
import com.hocs.server.api_spec_generator.domain.output.PathItem;
import com.hocs.server.api_spec_generator.domain.output.Schema;
import com.mongodb.bulk.BulkWriteResult;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

@Repository
public class OasRepositoryCustomImpl implements OasRepositoryCustom {

	@Autowired
	private MongoTemplate mongoTemplate;

	@Override
	public int bulkWrite(List<OAS> mergedEntities) {
		if (mergedEntities == null || mergedEntities.isEmpty()) {
			return 0;
		}

		// UNORDERED 모드로 BulkOperations 생성
		BulkOperations bulkOps = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED,
			OAS.class);

		for (OAS entity : mergedEntities) {
			// _id 필드를 기준으로 조건 작성
			Query query = new Query(Criteria.where("_id").is(entity.getId()));
			Update update = new Update();

			// pathList 업데이트: 각 key에 해당하는 리스트에 push
			if (entity.getPathList() != null && !entity.getPathList().isEmpty()) {
				for (Map.Entry<String, List<Map<String, PathItem>>> entry : entity.getPathList()
					.entrySet()) {
					List<Map<String, PathItem>> valueList = entry.getValue();
					if (valueList != null && !valueList.isEmpty()) {
						update.push("pathList." + entry.getKey()).each(valueList.toArray());
					}
				}
			}

			// schemasMap 업데이트: 각 key에 해당하는 리스트에 push
			if (entity.getSchemasMap() != null && !entity.getSchemasMap().isEmpty()) {
				for (Map.Entry<String, List<Schema>> entry : entity.getSchemasMap().entrySet()) {
					List<Schema> valueList = entry.getValue();
					if (valueList != null && !valueList.isEmpty()) {
						update.push("schemasMap." + entry.getKey()).each(valueList.toArray());
					}
				}
			}

			// info 업데이트
			if (entity.getInfo() != null) {
				update.set("info", entity.getInfo());
			}

			// 조건에 맞는 문서가 없으면 새로 생성 (upsert)
			bulkOps.upsert(query, update);
		}

		// 모든 bulk 연산 실행 후 성공 건수를 반환
		BulkWriteResult result = bulkOps.execute();
		return result.getModifiedCount() + result.getUpserts().size();
	}
}