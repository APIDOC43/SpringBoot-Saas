package com.hocs.server.saas_v2.legacy.saas.oas.controller;

import com.hocs.server.saas_v2.legacy.saas.oas.service.ApiDocsUiService;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * OAS 관련처리, static html 생성까지만 책임을 가지는 도메인
 */
@RestController
@Slf4j
public class OasController {
	@Value("${path.SSG-root}")
	private String renderRootPath;

	private final ApiDocsUiService apiDocsUiService;

	@Autowired
	public OasController(@Qualifier("hocsUIService") ApiDocsUiService apiDocsUiService) {
		this.apiDocsUiService = apiDocsUiService;
	}

	@PostMapping("/demo/api/v1/oas")
	public ResponseEntity<String> uploadFile(@RequestParam(name = "file") MultipartFile file,@RequestParam(name = "userId") String userId) {
		log.info("OasController.uploadFile");
		if (file.isEmpty()) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("파일이 없습니다.");
		}

		try {
			// 업로드된 파일을 저장할 경로 설정
			byte[] bytes = file.getBytes();
			Path path = Paths.get(renderRootPath+"/oas.yaml");
			Files.createDirectories(path.getParent());  // 디렉토리가 없을 경우 생성
			Files.write(path, bytes);

			apiDocsUiService.generateStaticHtml(userId);

			return ResponseEntity.status(HttpStatus.OK).body("파일 업로드 성공: " + file.getOriginalFilename());
		} catch (IOException e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("파일 업로드 중 오류 발생");
		}
	}


}
