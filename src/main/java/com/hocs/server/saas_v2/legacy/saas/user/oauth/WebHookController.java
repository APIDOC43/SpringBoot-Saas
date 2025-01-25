package com.hocs.server.saas_v2.legacy.saas.user.oauth;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

public class WebHookController {
	@PostMapping("/webhook")
	public ResponseEntity<String> handleWebhook(
		@RequestHeader("X-Hub-Signature-256") String signature,
		@RequestBody String payload) {
		System.out.println("Webhook received");
		// 시그니처 검증
		if (!verifySignature(signature, payload)) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid signature");
		}

		System.out.println("payload = " + payload);

		// 이벤트 처리 로직
		// TODO: payload를 파싱하여 커밋 정보 추출 및 처리

		return ResponseEntity.ok("Webhook received");
	}

	@CrossOrigin(origins = "http://192.168.0.103:9000")
	@GetMapping("/webhook")
	public String webhook() {
		System.out.println("Webhook received");
		return "webhook";
	}

	private boolean verifySignature(String signature, String payload) {
		// 시그니처 검증 로직 구현
		// TODO: HMAC SHA-256 알고리즘을 사용하여 검증
		return true;
	}
}
