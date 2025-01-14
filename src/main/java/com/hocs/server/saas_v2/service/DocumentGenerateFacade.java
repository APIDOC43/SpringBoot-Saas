package com.hocs.server.saas_v2.service;

import com.hocs.server.saas_v2.common.annotation.Facade;
import com.hocs.server.saas_v2.domain.ApiInfo;
import com.hocs.server.saas_v2.domain.ProjectMetaData;
import com.hocs.server.saas_v2.service.out.pipline.adapter.GenerateReceiptRequest;
import com.hocs.server.saas_v2.service.out.pipline.port.PiplineExecutorPort;
import java.util.List;
import lombok.RequiredArgsConstructor;

@Facade
@RequiredArgsConstructor
public class DocumentGenerateFacade {
	private final ProjectMetaDataService projectMetaDataService;
	private final PiplineExecutorPort piplineExecutorPort;

	public boolean generationReceipt(long metadataId, List<ApiInfo> excludeApiInfo) {
		ProjectMetaData metaData = projectMetaDataService.findMetadataById(metadataId);
		piplineExecutorPort.execute(new GenerateReceiptRequest(metaData,excludeApiInfo));

		return true;
	}
}