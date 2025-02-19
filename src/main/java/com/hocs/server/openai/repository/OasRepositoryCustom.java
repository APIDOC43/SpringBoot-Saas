package com.hocs.server.openai.repository;

import com.hocs.server.openai.domain.output.OAS;
import java.util.List;

public interface OasRepositoryCustom{
	void bulkWrite(List<OAS> mergedEntities);
}