package com.hocs.server.code_resolver.legacy.extractor.respository.mongo;

import com.hocs.server.code_resolver.legacy.extractor.domain.APISourceDependencyInfo;
import java.util.List;

public interface APISourceDependencyRepositoryCustom {
	void bulkWrite(List<APISourceDependencyInfo> mergedEntities);
}