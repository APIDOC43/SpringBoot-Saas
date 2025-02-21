package com.hocs.server.front_server.api.request;

import com.hocs.server.common.domain.ApiInfo;
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