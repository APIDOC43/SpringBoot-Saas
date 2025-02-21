package com.hocs.server.front_server.api.response;

import com.hocs.server.pipline.domain.ControllerFile;
import com.hocs.server.common.domain.ApiInfo;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.Data;

@Data
public class ApiInfoResponse {
	private int controllerCount = 0;
	private int apiCount = 0;
	private Long metadataId;
	private List<ControllerResponse> controllerResponses = new ArrayList<>();

	public ApiInfoResponse(int controllerCount, int apiCount, Long metadataId,
		Map<ControllerFile,List<ApiInfo>>  apiEndpointInfo) {
		this.controllerCount = controllerCount;
		this.apiCount = apiCount;
		this.metadataId = metadataId;

		for (ControllerFile controllerFile : apiEndpointInfo.keySet()) {
			List<ApiInfo> apiInfos = apiEndpointInfo.get(controllerFile);
			this.controllerResponses.add(new ControllerResponse(controllerFile,apiInfos));
		}
	}
}