package com.hocs.server.common.config;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class DebugController {

	private final MyCommandCounterListener listener;

	@GetMapping("/mongo/update-count")
	public String getUpdateCount() {
		return "Update count: " + listener.getUpdateCount();
	}

	@PostMapping("/mongo/reset-count")
	public void resetUpdateCount() {
		listener.reset();
	}
}