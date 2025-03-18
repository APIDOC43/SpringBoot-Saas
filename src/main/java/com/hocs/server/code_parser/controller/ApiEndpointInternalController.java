package com.hocs.server.code_parser.controller;

import com.hocs.server.code_parser.core.domain.APISourceDependencyInfo;
import com.hocs.server.code_parser.facade.ApiEndpointResolveFacade;
import com.hocs.server.common.domain.ProjectMetaData;
import com.hocs.server.pipline_orchestrator.domain.ControllerFile;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ApiEndpointInternalController {

	private final ApiEndpointResolveFacade facade;

	public APISourceDependencyInfo findAPIMetadata(String userId, ProjectMetaData metaData,
		String defaultBranchName, ControllerFile controllerFile, String requestId) {

		return facade.findAPIMetadata(userId, metaData, defaultBranchName, controllerFile, requestId);
	}

}
