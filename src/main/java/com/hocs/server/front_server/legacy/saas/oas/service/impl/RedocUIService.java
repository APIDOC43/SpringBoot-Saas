package com.hocs.server.front_server.legacy.saas.oas.service.impl;

import com.hocs.server.front_server.legacy.saas.oas.service.ApiDocsUiService;
import com.hocs.server.front_server.legacy.saas.util.cli.CLIManager;
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
