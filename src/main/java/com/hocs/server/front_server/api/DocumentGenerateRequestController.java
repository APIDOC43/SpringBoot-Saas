package com.hocs.server.front_server.api;

import com.hocs.server.front_server.api.request.FindApiInfoClientRequest;
import com.hocs.server.front_server.api.request.GenerateReceiptClientRequest;
import com.hocs.server.front_server.api.response.ApiInfoResponse;
import com.hocs.server.front_server.common.ApiResponse;
import com.hocs.server.front_server.facade.ApiEndpointFacade;
import com.hocs.server.front_server.facade.DocumentGenerateFacade;
import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/apis/document")
@RequiredArgsConstructor
@Slf4j
public class DocumentGenerateRequestController {

	private final ApiEndpointFacade apiEndpointFacade;
	private final DocumentGenerateFacade documentGenerateService;

	@PostMapping("/generation/reception/v1")
	public ResponseEntity<?> documentGenerationReceipt(@RequestBody @Valid GenerateReceiptClientRequest request) {
		long start = System.currentTimeMillis();

		String userId = UUID.randomUUID().toString();
		log.info("userId = " + userId);
		documentGenerateService.generationReceipt(userId,request.getMetadataId(),request.getExcludeApiInfo());

		long end = System.currentTimeMillis();

		System.out.println("esp time = " + (end-start));
		return ResponseEntity.status(HttpStatus.ACCEPTED).build();
	}

	@PostMapping("/endpoint/demo/v1")
	public ResponseEntity<?> findEndpointInfo(
		@RequestBody @Valid FindApiInfoClientRequest request) {
		ApiInfoResponse response = apiEndpointFacade.findEndpointInfo(request);

		return ResponseEntity.ok(ApiResponse.create(2000, response));
	}

	@PostMapping("/endpoint/demo/v1/test/dummy")
	public ResponseEntity<?> findEndpointInfoForTest(
		@RequestBody @Valid FindApiInfoClientRequest request,@RequestParam(name = "count") Integer count){

		List<ApiInfoResponse> list = new ArrayList<>();
		for (int i = 0; i < count; i++) {
			list.add(apiEndpointFacade.findEndpointInfo(request));
		}

		return ResponseEntity.ok(ApiResponse.create(2000, list.get(0)));
	}
}
