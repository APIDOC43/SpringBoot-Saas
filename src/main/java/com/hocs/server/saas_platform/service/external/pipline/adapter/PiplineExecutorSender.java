package com.hocs.server.saas_platform.service.external.pipline.adapter;

import com.hocs.server.common.domain.ApiInfo;
import com.hocs.server.common.domain.DocGeneratePiplineRequest;
import com.hocs.server.pipline_orchestrator.facade.PiplineIngressFacade;
import com.hocs.server.saas_platform.common.annotation.Adapter;
import com.hocs.server.saas_platform.service.external.pipline.port.PiplineExecutorPort;
import java.util.List;
import lombok.RequiredArgsConstructor;

@Adapter
@RequiredArgsConstructor
public class PiplineExecutorSender implements PiplineExecutorPort {

	private final PiplineIngressFacade piplineIngressFacade;

	@Override
	public void send(DocGeneratePiplineRequest docGeneratePiplineRequest, List<ApiInfo> excludeApiInfo) {
		piplineIngressFacade.ingress(docGeneratePiplineRequest, excludeApiInfo);
	}
}