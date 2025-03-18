package com.hocs.server.saas_platform.service.impl;

import com.hocs.server.saas_platform.service.ApiDocsUiService;
import com.hocs.server.saas_platform.service.CLIManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RedocUIService implements ApiDocsUiService {

	private final CLIManager cliManager;

	@Override
	public void generateStaticHtml(String userId) {
		String[] command = {"redocly", "build-docs", "uploads/openapi3.yaml","-o", "src/main/resources/static/redoc-static.html"};

		cliManager.executeCommand(command);
	}
}
