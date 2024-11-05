package com.hocs.server.saas.demo.controller;

import com.hocs.server.saas.demo.service.DemoFacadeService;
import com.hocs.server.saas.user.gitapi.domin.GitRepo;
import com.hocs.server.saas.apidoc.controller.StaticApiDocService;
import com.hocs.server.openai.util.HttpClient;
import com.hocs.server.saas.user.oauth.dto.FilesData;
import com.hocs.server.saas.user.oauth.dto.GetContentRequest;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequiredArgsConstructor
@Slf4j

public class DemoController {

	private final DemoFacadeService demoService;
	private final StaticApiDocService staticApiDocService;


	@GetMapping("/demo/clone/{userId}")
	public String demoClone2(@RequestParam("repoUrl") String repoUrl,
		@PathVariable(name = "userId") String userId, Model model)
		throws Exception {

		demoService.generateApiDoc(GitRepo.of(repoUrl),userId,model);
		return "redirect:/demo/layout?userId=" + userId;
	}
	@GetMapping("/demo")
	public String demo(Model model) {
		log.info("HomeController.demo---");
		return "demo-main";
	}

	@GetMapping("/demo/layout")
	public String getLayOut(@RequestParam(name = "userId") String userId, Model model)
		throws IOException {
		try {
			List<FilesData> htmlFiles = staticApiDocService.loadApiDocLoadToFilesData(
				userId);
			model.addAttribute("htmlFiles", htmlFiles);
			String response = HttpClient.findHtmlRequest(htmlFiles.get(0).getFilePath());
			model.addAttribute("content", response);
		} catch (NoSuchFileException e) {
			return "redirect:/demo/error/busy";
		}
		return "layout";
	}


	@PostMapping("/demo/content")
	@ResponseBody
	public String getFileContent(@RequestBody GetContentRequest request) throws IOException {
		Path filePath = staticApiDocService.getContent(request);
		log.info("filePath.toString() = " + filePath.toString());

		return Files.readString(filePath); // 파일 내용을 문자열로 읽어 반환
	}


}
