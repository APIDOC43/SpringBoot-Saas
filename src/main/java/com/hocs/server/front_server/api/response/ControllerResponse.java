package com.hocs.server.front_server.api.response;


import com.hocs.server.api_doc_pipline.domain.ControllerFile;
import com.hocs.server.common.domain.ApiInfo;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ControllerResponse {
	private ControllerFile controllerFile;
	private List<ApiInfo> apiInfos;
}