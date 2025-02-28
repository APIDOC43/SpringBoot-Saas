package com.hocs.server.openai.repository;

import com.hocs.server.openai.domain.output.OAS;
import com.hocs.server.openai.domain.output.PathItem;
import com.hocs.server.openai.domain.output.Schema;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class OasRepositoryCustomImpl implements OasRepositoryCustom {

	@Autowired
	private MongoTemplate mongoTemplate;

	@Override
	@Transactional
	public void bulkWrite(List<OAS> mergedEntities) {
		// UNORDERED 모드로 BulkOperations 생성
		BulkOperations bulkOps = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, OAS.class);

		for (OAS entity : mergedEntities) {
			// _id 필드를 기준으로 조건 작성
			Query query = new Query(Criteria.where("_id").is(entity.getId()));
			Update update = new Update();

			// pathList 업데이트: 각 key에 해당하는 리스트에 push
			if (entity.getPathList() != null && !entity.getPathList().isEmpty()) {
				for (Map.Entry<String, List<Map<String, PathItem>>> entry : entity.getPathList().entrySet()) {
					List<Map<String, PathItem>> valueList = entry.getValue();
					if (valueList != null && !valueList.isEmpty()) {
						// "pathList.<key>" 필드에 리스트의 각 요소를 추가
						update.push("pathList." + entry.getKey()).each(valueList.toArray());
					}
				}
			}

			// schemasMap 업데이트: 각 key에 해당하는 리스트에 push
			if (entity.getSchemasMap() != null && !entity.getSchemasMap().isEmpty()) {
				for (Map.Entry<String, List<Schema>> entry : entity.getSchemasMap().entrySet()) {
					List<Schema> valueList = entry.getValue();
					if (valueList != null && !valueList.isEmpty()) {
						// "schemasMap.<key>" 필드에 리스트의 각 요소를 추가
						update.push("schemasMap." + entry.getKey()).each(valueList.toArray());
					}
				}
			}

			if (entity.getInfo() != null) {
				update.set("info", entity.getInfo());
			}

			// 조건에 맞는 문서가 없으면 새로 생성 (upsert)
			bulkOps.upsert(query, update);
		}
		// 모든 bulk 연산 실행
		bulkOps.execute();
	}
}