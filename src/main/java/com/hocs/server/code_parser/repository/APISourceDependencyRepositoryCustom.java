package com.hocs.server.code_parser.repository;

import com.hocs.server.code_parser.core.domain.APISourceDependencyInfo;
import java.util.List;

public interface APISourceDependencyRepositoryCustom {
	List<APISourceDependencyInfo> bulkWrite(List<APISourceDependencyInfo> mergedEntities);
}