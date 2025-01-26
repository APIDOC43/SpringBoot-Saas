package com.hocs.server.saas_v2.api.request;

import com.hocs.server.common.ApiInfo;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GenerateReceiptClientRequest {
	@NotNull
	private long metadataId;
	private List<ApiInfo> excludeApiInfo;
}