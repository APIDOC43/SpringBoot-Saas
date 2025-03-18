package com.hocs.server.saas_platform.controller.request;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
class GenerateStaticHtmlRequest {

	MultipartFile file;
	String userId;
}
