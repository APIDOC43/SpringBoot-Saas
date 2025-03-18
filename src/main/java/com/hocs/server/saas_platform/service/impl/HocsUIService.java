package com.hocs.server.saas_platform.service.impl;

import com.hocs.server.saas_platform.service.ApiDocsUiService;
import com.hocs.server.saas_platform.service.CLIManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class HocsUIService implements ApiDocsUiService {

	@Value("${path.SSG-root}")
	private String renderPath;

	private final CLIManager cliManager;

	@Override
	public void generateStaticHtml(String userId) {
		log.info("HocsUIService.generateStaticHtml");

		// npm run build-docs --prefix /Users/hong/Desktop/hong/soma/alone/rander --userId=userId
		String[] command = {"npm", "run","build-docs","--prefix", renderPath,"--","--userId="+userId};
		cliManager.executeCommand(command);
	}
}


