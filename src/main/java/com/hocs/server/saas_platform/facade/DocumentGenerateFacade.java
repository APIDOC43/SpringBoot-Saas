package com.hocs.server.saas_platform.facade;

import com.hocs.server.common.domain.ApiInfo;
import com.hocs.server.common.domain.DocGeneratePiplineTask;
import com.hocs.server.common.domain.ProjectMetaData;
import com.hocs.server.saas_platform.common.annotation.Facade;
import com.hocs.server.saas_platform.service.DocGeneratePiplineTaskService;
import com.hocs.server.saas_platform.service.ProjectMetaDataService;
import com.hocs.server.saas_platform.service.external.pipline.port.PiplineExecutorPort;
import java.util.List;
import lombok.RequiredArgsConstructor;

@Facade
@RequiredArgsConstructor
public class DocumentGenerateFacade {
	private final ProjectMetaDataService projectMetaDataService;
	private final PiplineExecutorPort piplineExecutorPort;
	private final DocGeneratePiplineTaskService docGeneratePiplineTaskService;

	public boolean generationReceipt(String userId, long metadataId, List<ApiInfo> excludeApiInfo) {
		ProjectMetaData metaData = projectMetaDataService.findMetadataById(metadataId);

		DocGeneratePiplineTask docGeneratePiplineTask = new DocGeneratePiplineTask(userId,
			metaData);

		docGeneratePiplineTaskService.save(docGeneratePiplineTask);

		piplineExecutorPort.send(docGeneratePiplineTask,excludeApiInfo);

		return true;
	}
}