package com.hocs.server.saas_v2.facade;

import com.hocs.server.saas_v2.common.annotation.Facade;
import com.hocs.server.saas_v2.domain.ApiInfo;
import com.hocs.server.saas_v2.domain.ProjectMetaData;
import com.hocs.server.saas_v2.service.ProjectMetaDataService;
import com.hocs.server.saas_v2.service.out.pipline.adapter.GenerationRequest;
import com.hocs.server.saas_v2.service.out.pipline.port.PiplineExecutorPort;
import java.util.List;
import lombok.RequiredArgsConstructor;

@Facade
@RequiredArgsConstructor
public class DocumentGenerateFacade {
	private final ProjectMetaDataService projectMetaDataService;
	private final PiplineExecutorPort piplineExecutorPort;

	/**
	 * 메타데이터 조회 후
	 * @param metadataId
	 * @param excludeApiInfo
	 * @return
	 */
	public boolean generationReceipt(String userId, long metadataId, List<ApiInfo> excludeApiInfo) {
		ProjectMetaData metaData = projectMetaDataService.findMetadataById(metadataId);
		piplineExecutorPort.send(new GenerationRequest(userId,metaData,excludeApiInfo));

		return true;
	}
}