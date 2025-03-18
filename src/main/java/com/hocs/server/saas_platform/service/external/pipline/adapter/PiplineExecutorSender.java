package com.hocs.server.saas_platform.service.external.pipline.adapter;

import com.hocs.server.common.domain.ApiInfo;
import com.hocs.server.common.domain.DocGeneratePiplineTask;
import com.hocs.server.pipline_orchestrator.PiplineExecutor;
import com.hocs.server.saas_platform.common.annotation.Adapter;
import com.hocs.server.saas_platform.service.external.pipline.port.PiplineExecutorPort;
import java.util.List;
import lombok.RequiredArgsConstructor;

@Adapter
@RequiredArgsConstructor
public class PiplineExecutorSender implements PiplineExecutorPort {

	private final PiplineExecutor piplineExecutor;

	@Override
	public void send(DocGeneratePiplineTask docGeneratePiplineTask, List<ApiInfo> excludeApiInfo) {
		piplineExecutor.receive(docGeneratePiplineTask, excludeApiInfo);
	}
}