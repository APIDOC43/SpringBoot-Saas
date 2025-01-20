package com.hocs.server.saas_v2.service.out.pipline.port;

import com.hocs.server.saas_v2.service.out.pipline.adapter.GenerationRequest;

public interface PiplineExecutorPort {

	void send(GenerationRequest request);
}
