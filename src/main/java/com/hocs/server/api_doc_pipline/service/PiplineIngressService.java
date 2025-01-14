package com.hocs.server.api_doc_pipline.service;

import com.hocs.server.saas_v2.service.out.pipline.adapter.GenerateReceiptRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PiplineIngressService {

	public void start(GenerateReceiptRequest request) {
		//pipline start. 파이프라인 진입점.
	}
}