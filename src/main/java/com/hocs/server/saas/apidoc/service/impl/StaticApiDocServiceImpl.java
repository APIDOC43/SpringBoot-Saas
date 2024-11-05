package com.hocs.server.saas.apidoc.service.impl;

import com.hocs.server.saas.apidoc.service.StaticApiDocService;
import com.hocs.server.saas.user.oauth.dto.FilesData;
import com.hocs.server.saas.user.oauth.dto.GetContentRequest;
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


@Service
@RequiredArgsConstructor
@Slf4j
public class StaticApiDocServiceImpl implements StaticApiDocService {
	@Value("${path.generate-doc-html-dir}")
	private String HTML_DIR;

	@Override
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
		return Paths.get(HTML_DIR, request.getFilename());
	}


}
