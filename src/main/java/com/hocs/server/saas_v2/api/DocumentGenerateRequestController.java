package com.hocs.server.saas_v2.api;

import com.hocs.server.saas_v2.api.request.GenerationRequest;
import com.hocs.server.saas_v2.service.DocumentGenerateRequestService;
import jakarta.validation.Valid;
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

	private final DocumentGenerateRequestService service;

	@PostMapping("/generation/receipt/v1")
	public ResponseEntity<?> documentGenerationReceipt(@RequestBody @Valid GenerationRequest request) {



	    return ResponseEntity.status(HttpStatus.ACCEPTED).build();
	}
}
