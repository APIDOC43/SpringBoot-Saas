package com.hocs.server.saas_v2.api.request;

import com.hocs.server.saas_v2.domain.ApiInfo;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GenerateReceiptClientRequest {
	private long metadataId;
	private List<ApiInfo> excludeApiInfo;
}