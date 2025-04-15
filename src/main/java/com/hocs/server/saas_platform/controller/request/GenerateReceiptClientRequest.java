package com.hocs.server.saas_platform.controller.request;

import com.hocs.server.common.domain.ApiInfo;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GenerateReceiptClientRequest {
	@NotNull
	private Long metadataId;
	private List<ApiInfo> excludeApiInfo;
}