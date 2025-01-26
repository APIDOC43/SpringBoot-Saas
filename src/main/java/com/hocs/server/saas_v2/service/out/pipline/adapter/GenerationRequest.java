package com.hocs.server.saas_v2.service.out.pipline.adapter;

import com.hocs.server.common.ApiInfo;
import com.hocs.server.common.ProjectMetaData;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GenerationRequest {
	private String userId;
	private ProjectMetaData metaData;
	private List<ApiInfo> excludeApiInfo;
}