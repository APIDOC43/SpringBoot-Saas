package com.hocs.server.saas_v2.service.out.pipline.adapter;

import com.hocs.server.api_doc_pipline.service.PiplineIngressService;
import com.hocs.server.saas_v2.common.annotation.Adapter;
import com.hocs.server.saas_v2.service.out.pipline.port.PiplineExecutorPort;
import lombok.RequiredArgsConstructor;

@Adapter
@RequiredArgsConstructor
public class PiplineExecutorAdapter implements PiplineExecutorPort {

	private final PiplineIngressService piplineIngressService;
	@Override
	public void execute(GenerateReceiptRequest request) {
		piplineIngressService.start(request);
	}
}