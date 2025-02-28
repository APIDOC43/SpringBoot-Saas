package com.hocs.server.custom_rag.legacy.extractor.respository.mongo;

import com.hocs.server.custom_rag.legacy.extractor.domain.APISourceDependencyInfo;
import java.util.List;

public interface APISourceDependencyRepositoryCustom {
	int bulkWrite(List<APISourceDependencyInfo> mergedEntities);
}