package com.hocs.server.api_spec_generator.repository;

import com.hocs.server.api_spec_generator.domain.output.OAS;
import java.util.List;

public interface OasRepositoryCustom{
	void bulkWrite(List<OAS> mergedEntities);
}