package com.hocs.server.saas_platform.service.external.pipline.port;

import com.hocs.server.common.domain.ApiInfo;
import com.hocs.server.common.domain.DocGeneratePiplineRequest;
import java.util.List;

public interface PiplineExecutorPort {

	void send(DocGeneratePiplineRequest docGeneratePiplineRequest, List<ApiInfo> excludeApiInfo);

}
