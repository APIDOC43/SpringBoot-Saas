package com.hocs.server.saas_v2.service.out.pipline.adapter;

import com.hocs.server.saas_v2.domain.ApiInfo;
import com.hocs.server.saas_v2.domain.ProjectMetaData;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GenerateReceiptRequest {
	private ProjectMetaData metaData;
	private List<ApiInfo> excludeApiInfo;
}