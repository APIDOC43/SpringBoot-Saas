package com.hocs.server.front_server.legacy.saas.oas.dto.request;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
class GenerateStaticHtmlRequest {

	MultipartFile file;
	String userId;
}
