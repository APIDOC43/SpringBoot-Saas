package com.hocs.server.saas_v2.api;

import com.hocs.server.saas_v2.api.request.FindApiInfoClientRequest;
import com.hocs.server.saas_v2.api.response.ApiInfoResponse;
import com.hocs.server.saas_v2.common.ApiResponse;
import com.hocs.server.saas_v2.service.ApiEndpointFacade;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/apis/document")
@RequiredArgsConstructor
public class DocumentGenerateRequestController {

	private final ApiEndpointFacade documentGenerateFacade;

	@PostMapping("/generation/receipt/v1")
	public ResponseEntity<?> documentGenerationReceipt(@RequestBody @NotNull Long metadataId) {

		return ResponseEntity.status(HttpStatus.ACCEPTED).build();
	}

	@PostMapping("/endpoint/demo/v1")
	public ResponseEntity<?> findEndpointInfo(
		@RequestBody @Valid FindApiInfoClientRequest request) {
		ApiInfoResponse response = documentGenerateFacade.findEndpointInfo(request);

		return ResponseEntity.ok(ApiResponse.create(2000, response));
	}
}
