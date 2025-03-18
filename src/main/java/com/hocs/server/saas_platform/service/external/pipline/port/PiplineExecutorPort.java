package com.hocs.server.saas_platform.service.external.pipline.port;

import com.hocs.server.common.domain.ApiInfo;
import com.hocs.server.common.domain.DocGeneratePiplineTask;
import java.util.List;

public interface PiplineExecutorPort {

	void send(DocGeneratePiplineTask docGeneratePiplineTask, List<ApiInfo> excludeApiInfo);

}
