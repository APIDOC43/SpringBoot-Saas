package com.hocs.server.saas.apidoc.controller;

import com.hocs.server.saas.user.oauth.dto.FilesData;
import com.hocs.server.saas.user.oauth.dto.GetContentRequest;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class StaticApiDocService {
	@Value("${path.generate-doc-html-dir}")
	private String HTML_DIR;

	public List<FilesData> loadApiDocLoadToFilesData(String userId) throws IOException {
		List<FilesData> htmlFiles = Files.list(
				Paths.get(HTML_DIR + "/" + userId + "/dist/apis"))
			.filter(Files::isRegularFile)
			.filter(path -> path.toString().endsWith(".html"))
			.map(path -> new FilesData(
				userId + "/dist/apis/" + path.getFileName().toString()))
			.collect(Collectors.toList());
		return htmlFiles;
	}

	public Path getContent(GetContentRequest request) {
		return Paths.get(HTML_DIR, request.getFilename());
	}
}
