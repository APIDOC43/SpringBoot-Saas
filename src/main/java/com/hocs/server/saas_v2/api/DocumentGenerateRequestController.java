package com.hocs.server.saas_v2.api;

import com.hocs.server.saas_v2.api.request.FindApiInfoClientRequest;
import com.hocs.server.saas_v2.api.request.GenerateReceiptClientRequest;
import com.hocs.server.saas_v2.api.response.ApiInfoResponse;
import com.hocs.server.saas_v2.common.ApiResponse;
import com.hocs.server.saas_v2.facade.ApiEndpointFacade;
import com.hocs.server.saas_v2.facade.DocumentGenerateFacade;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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
		String userId = UUID.randomUUID().toString();
		log.info("userId = " + userId);
		documentGenerateService.generationReceipt(userId,request.getMetadataId(),request.getExcludeApiInfo());

		return ResponseEntity.status(HttpStatus.ACCEPTED).build();
	}

	@PostMapping("/endpoint/demo/v1")
	public ResponseEntity<?> findEndpointInfo(
		@RequestBody @Valid FindApiInfoClientRequest request) {
		ApiInfoResponse response = apiEndpointFacade.findEndpointInfo(request);

		return ResponseEntity.ok(ApiResponse.create(2000, response));
	}
}
