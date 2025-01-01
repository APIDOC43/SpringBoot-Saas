package com.hocs.server.code_resolver.service;

import com.hocs.server.code_resolver.collector.domain.LanguageFramework;
import com.hocs.server.code_resolver.collector.domain.LanguageFrameworkFactory;
import com.hocs.server.code_resolver.extractor.ApiInfoExtractorService;
import com.hocs.server.saas_v2.domain.ApiInfo;
import com.hocs.server.saas_v2.domain.ClientProjectPath;
import com.hocs.server.saas_v2.service.out.ApiEndpointCollector.adapter.FindApiInfoApiRequest;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ApiEndpointCollectorService {

	private final ApiInfoExtractorService ApiInfoExtractorService;

	public Map<String, List<ApiInfo>> findApiInfo(FindApiInfoApiRequest request){
		LanguageFramework languageFramework = LanguageFrameworkFactory.create(request.getLanguage(),
			request.getProjectFramework());

		int firstPageSize = request.getFirstPageSize();
		ClientProjectPath path = request.getPath();

		List<File> files = collectFiles(path.getUrl().toFile(), languageFramework.getExtension());

		List<File> controllers = files.stream()
			.filter(f -> languageFramework.isApiEntry(f.toPath()))
			.collect(Collectors.toList());

		return ApiInfoExtractorService.extractApiInfo(controllers);
	}


	public List<File> collectFiles(File dir, String srcSuffix) {
		List<File> javaFiles = new ArrayList<>();

		for (File file : Objects.requireNonNull(dir.listFiles())) {
			if (file.isDirectory()) {
				javaFiles.addAll(collectFiles(file,srcSuffix));
			} else {
				if (file.getName().endsWith("."+srcSuffix)) {
					javaFiles.add(file);
				}
			}
		}

		return javaFiles;
	}
}