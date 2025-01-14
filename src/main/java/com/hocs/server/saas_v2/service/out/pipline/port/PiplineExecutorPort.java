package com.hocs.server.saas_v2.service.out.pipline.port;

import com.hocs.server.saas_v2.service.out.pipline.adapter.GenerateReceiptRequest;

public interface PiplineExecutorPort {

	void execute(GenerateReceiptRequest request);
}
