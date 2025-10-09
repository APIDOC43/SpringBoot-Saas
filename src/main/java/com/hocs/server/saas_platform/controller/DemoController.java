package com.hocs.server.saas_platform.controller;



import com.hocs.server.api_spec_generator.domain.output.OAS;
import com.hocs.server.api_spec_generator.service.OasIntegrationService;
import com.hocs.server.pipline_orchestrator.service.out.OasSendClient;
import com.hocs.server.saas_platform.controller.dto.ProgressDto;
import com.hocs.server.saas_platform.controller.request.GetContentRequest;
import com.hocs.server.saas_platform.domain.FilesData;
import com.hocs.server.saas_platform.service.GenerationService;
import com.hocs.server.saas_platform.service.OasService;
import com.hocs.server.saas_platform.service.StaticApiDocService;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequiredArgsConstructor
@Slf4j
public class DemoController {

	private final OasSendClient oasSendClient;
	private final StaticApiDocService apiDocService;
	private final OasService oasService;

	@GetMapping("/demo")
	public String demo(Model model) {
		log.info("HomeController.demo---");
		List<Map<String, String>> repoList = Arrays.asList(
			Map.of(
				"url", "https://github.com/osopromadze/Spring-Boot-Blog-REST-API.git",
				"name", "mosopromadze/Spring-Boot-Blog-REST-API",
				"description", "Steps to Setup · 1. Clone the application · 2. Create Mysql database · 3. Change mysql username and password as per your installation · 4. Run the app using maven ..."
			),
			Map.of(
				"url", "https://github.com/givanthak/spring-boot-rest-api-tutorial.git",
				"name", "givanthak/spring-boot-rest-api-tutorial",
				"description", "1. Clone the application · 2. Create Mysql database · 3. Change mysql username and password as per your installation · 4. Build and run the app using maven ..."
			),
			Map.of(
				"url", "https://github.com/bezkoder/spring-boot-3-rest-api-example.git",
				"name", "bezkoder/spring-boot-3-rest-api-example",
				"description", "In this tutorial, we're gonna build a Spring Boot 3 Rest API example with Maven that implement CRUD operations. You'll know: Way to define Spring Rest ..."
			),
			Map.of(
				"url", "https://github.com/khoubyari/spring-boot-rest-example.git",
				"name", "khoubyari/spring-boot-rest-example",
				"description", "This is a sample Java / Maven / Spring Boot (version 1.5.6) application that can be used as a starter for creating a microservice complete with built-in health ..."
			)
		);
		model.addAttribute("repoList", repoList);
		return "demo-main";
	}

	@ResponseBody
	@GetMapping("/demo/layout")
	public List<OAS> getLayOut(
			@RequestParam(name = "userId") String userId,
			@RequestParam(name = "index") Integer index,
			Model model) {

		List<OAS> oasList = oasService.findAllByUserId(userId);
		return oasList;
	}

	@Deprecated
	@GetMapping("/demo/layout/v1")
	public String getLayOutV1(
		@RequestParam(name = "userId") String userId,
		@RequestParam(name = "index") Integer index,
		Model model) {
		try {
			List<FilesData> htmlFiles = apiDocService.findApiListByUserId(userId);

			model.addAttribute("htmlFiles", htmlFiles);
			String response = oasSendClient.findHtmlRequest(htmlFiles.get(index).getFilePath());
			model.addAttribute("content", response);
		} catch (Exception e) {
			e.printStackTrace();
			return "redirect:/demo/error/busy";
		}
		return "layout";
	}


	@PostMapping("/demo/content")
	@ResponseBody
	public String getFileContent(@RequestBody GetContentRequest request) throws IOException {
		Path filePath = apiDocService.getContent(request);
		log.info("filePath.toString() = " + filePath.toString());

		return Files.readString(filePath); // 파일 내용을 문자열로 읽어 반환
	}

	@GetMapping("/demo/api/selection")
	public String selectionView(@RequestParam(value = "repoUrl") String repoUrl, Model model) {
		model.addAttribute("repoUrl",repoUrl);
		return "select-api";
	}

	@GetMapping("/list/v1")
	public String getList(
		@RequestParam("metadataId") Long metadataId
	) {
		return "progress";
	}
}



