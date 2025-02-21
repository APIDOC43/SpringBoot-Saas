package com.hocs.server.front_server.service.out.pipline.adapter;

import com.hocs.server.api_doc_pipline.PiplineExecutionReceiver;
import com.hocs.server.common.domain.ApiInfo;
import com.hocs.server.front_server.common.annotation.Adapter;
import com.hocs.server.common.domain.DocGeneratePiplineTask;
import com.hocs.server.front_server.service.out.pipline.port.PiplineExecutorPort;
import java.util.List;
import lombok.RequiredArgsConstructor;

@Adapter
@RequiredArgsConstructor
public class PiplineExecutorSender implements PiplineExecutorPort {

	private final PiplineExecutionReceiver piplineExecutionReceiver;

	@Override
	public void send(DocGeneratePiplineTask docGeneratePiplineTask, List<ApiInfo> excludeApiInfo) {
		piplineExecutionReceiver.receive(docGeneratePiplineTask, excludeApiInfo);
	}
}