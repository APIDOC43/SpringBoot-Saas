package com.hocs.server.saas_platform.controller.response;


import com.hocs.server.common.domain.ApiInfo;
import com.hocs.server.pipline_orchestrator.domain.ControllerFile;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ControllerResponse {
	private ControllerFile controllerFile;
	private List<ApiInfo> apiInfos;
}