package com.hocs.server.saas_v2.api.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;


@Data
public class GenerationRequest {
	@NotNull
	private CodingLanguage language;
	@NotNull
	private ProjectFramework projectFramework;
	@NotNull @NotBlank
	private String coreSrcRootPath;
	private String userId; //or accessToken
}
