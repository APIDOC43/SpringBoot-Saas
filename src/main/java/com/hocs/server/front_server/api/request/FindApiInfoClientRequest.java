package com.hocs.server.front_server.api.request;

import com.hocs.server.common.domain.CodingLanguage;
import com.hocs.server.common.domain.ProjectFramework;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;


@Data
public class FindApiInfoClientRequest {
	@NotNull
	private CodingLanguage language;
	@NotNull
	private ProjectFramework projectFramework;
	@NotNull @NotBlank
	private String gitCloneUrl;
	@NotNull @NotBlank
	private String coreSrcRootPath;
	private String userId; //or accessToken
}
