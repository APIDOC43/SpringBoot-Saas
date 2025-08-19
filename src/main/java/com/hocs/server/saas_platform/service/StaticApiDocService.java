package com.hocs.server.saas_platform.service;

import com.hocs.server.saas_platform.controller.request.GetContentRequest;
import com.hocs.server.saas_platform.domain.FilesData;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

//deprecated

@Service
@RequiredArgsConstructor
@Slf4j
public class StaticApiDocService {
	@Value("${path.generate-doc-html-dir}")
	private String HTML_DIR;

	public List<FilesData> findApiListByUserId(String userId)  {
		List<FilesData> htmlFiles = null;
		try {
			htmlFiles = Files.list(
					Paths.get(HTML_DIR + "/" + userId + "/dist/apis"))
				.filter(Files::isRegularFile)
				.filter(path -> path.toString().endsWith(".html"))
				.map(path -> new FilesData(
					userId + "/dist/apis/" + path.getFileName().toString()))
				.collect(Collectors.toList());
		} catch (IOException e) {
			e.printStackTrace();
			log.error("Error while loading api doc files", e);
		}
		return htmlFiles;
	}

	public Path getContent(GetContentRequest request) {
		if (request.getFilename() == null) {
			return Paths.get(HTML_DIR);
		}
		return Paths.get(HTML_DIR, request.getFilename());
	}


}
