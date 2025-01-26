package com.hocs.server.saas_v2.service.out.pipline.adapter;

import com.hocs.server.api_doc_pipline.PiplineExecutionReceiver;
import com.hocs.server.saas_v2.common.annotation.Adapter;
import com.hocs.server.saas_v2.service.out.pipline.port.PiplineExecutorPort;
import lombok.RequiredArgsConstructor;

@Adapter
@RequiredArgsConstructor
public class PiplineExecutorSender implements PiplineExecutorPort {

	private final PiplineExecutionReceiver piplineExecutionReceiver;
	@Override
	public void send(GenerationRequest request) {
		piplineExecutionReceiver.receive(request);
	}
}